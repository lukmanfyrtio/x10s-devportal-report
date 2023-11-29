package com.wso2.swamedia.reportusageapi.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;
import com.wso2.swamedia.reportusageapi.utils.Utils;

@RestController
@RequestMapping("/report")
public class ReportUsageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageController.class);

	@Autowired
	private ReportUsageService reportUsageService;

	@GetMapping("/monthly-summary")
	public ResponseEntity<?> getMonthlySummary(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String applicationId,
			@RequestParam(required = false) String organization, @RequestParam(required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, @RequestParam(required = false) String apiId,
			@RequestParam(required = false, defaultValue = "false") Boolean showDeletedSubscription,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		 String organizationToken = Utils.getOrganization(principal.getAttributes());

		LOGGER.info("Received request for monthly summary");
		try {

			ApiResponse<?> response = ApiResponse.success("Monthly summary retrieval successful.",
					reportUsageService.getMonthlyReport(year, month, applicationId, apiId, organizationToken, page, size, search,
							organization, showDeletedSubscription,keyType));
			LOGGER.info("Monthly summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}

	}

	@GetMapping("/monthly-summary/details")
	public ResponseEntity<?> getApiDataUsage(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(value = "applicationId") String applicationId,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(required = false, defaultValue = "false") Boolean showDeletedSubscription,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		LOGGER.info("Received request for API Monthly detail log");
		try {


			Map<String, Object> total = reportUsageService.totalMonthlyDetailLog(Utils.getOrganization(), applicationId, apiId, search,
					year, month, showDeletedSubscription, keyType);
			Pageable pageable = PageRequest.of(page, size);
			LinkedHashMap<String, Object> result = new LinkedHashMap<>();
			result.put("requestCount", total.get("request_count"));
			result.put("requestOK", total.get("count_200"));
			result.put("requestNOK", total.get("count_not_200"));
			result.put("details", reportUsageService.getMonthlyDetailLogReport(Utils.getOrganization(), applicationId, apiId, search,
					pageable, year, month, showDeletedSubscription, keyType));

			ApiResponse<?> response = ApiResponse.success("Monthly detail log retrieval successful.", result);

			LOGGER.info("API Monthly detail log retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resource-summary")
	public ResponseEntity<?> getResourceSummary(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String resource,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(required = false, defaultValue = "false") Boolean showDeletedSubscription,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {
		
		LOGGER.info("Received request for resource summary");
		try {
			ApiResponse<?> response = ApiResponse.success("Resource summary retrieval successful.",
					reportUsageService.getResourceReport(year, month, resource, apiId, Utils.getOrganization(), page, size, search,
							showDeletedSubscription,keyType));

			LOGGER.info("Resource summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resource-summary/details")
	public ResponseEntity<?> getResourceDetailLog(@RequestParam(value = "resource") String resource,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(required = false, defaultValue = "false") Boolean showDeletedSubscription,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		LOGGER.info("Received request for resource detail log");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Resource detail log retrieval successful.",
					reportUsageService.getDetailLogResourceSum(Utils.getOrganization(), resource, apiId, search, pageable,
							showDeletedSubscription,keyType));

			LOGGER.info("Resource detail log retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api")
	public ResponseEntity<?> getBackendAPIUsage(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String apiId,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, 
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();

		if (!Utils.isAdmin(principal.getAttributes())) {
			ApiResponse<Resource> response = ApiResponse.error("You do not have permission to access this API.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		LOGGER.info("Received request for backend api usage summary");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Backend api usage summary retrieval successful.",
					reportUsageService.getBackendAPIUsage(Utils.getOrganization(), year, month, apiId, search, pageable, keyType));

			LOGGER.info("Backend api usage summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/error-summary")
	public ResponseEntity<?> getErrorSummary(@RequestParam(required = false) String apiId,
			@RequestParam(required = false) String version, @RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "false") boolean asPercent,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, 
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		if (!Utils.isAdmin(principal.getAttributes())) {
			ApiResponse<Resource> response = ApiResponse.error("You do not have permission to access this API.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		LOGGER.info("Received request for Error summary ");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Error summary retrieval successful.",
					reportUsageService.getErrorSummary(apiId, version, asPercent, search, pageable,keyType));

			LOGGER.info("Error summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api/details")
	public ResponseEntity<?> getBackendAPIUsageDetails(@RequestParam(required = false) String apiId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, 
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		if (!Utils.isAdmin(principal.getAttributes())) {
			ApiResponse<Resource> response = ApiResponse.error("You do not have permission to access this API.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		LOGGER.info("Received request for backend api usage summary details");
		try {
			if (apiId == null) {
				ApiResponse<?> response = ApiResponse.error("apiId parameters is required.");
				return ResponseEntity.badRequest().body(response);
			}
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Backend api usage summary details retrieval successful.",
					reportUsageService.getBackendAPIUsageDetails(apiId, pageable, keyType));

			LOGGER.info("Backend api usage summary details retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/apis")
	public ResponseEntity<?> getListAPIName(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "organization", required = false) String organization) {
		LOGGER.info("Received request to get the list of API names");
		
		
		try {
			List<Map<String, Object>> apiNames = reportUsageService.getApis(username, organization);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse.success("API names retrieved successfully",
					apiNames);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API names: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resources")
	public ResponseEntity<?> getListAPIResource(@RequestParam(value = "apiId") String apiId,
			Authentication authentication) {
		
		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		String username = Utils.isAdmin(principal.getAttributes()) ? null
				: principal.getAttributes().get("http://wso2.org/claims/username").toString();
		
		LOGGER.info("Received request to get the list of resources for API with ID: {}", apiId);
		try {
			List<Map<String, Object>> apiResources = reportUsageService.getApiResourceByAPI(username, apiId);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse
					.success("API resources retrieved successfully", apiResources);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API resources: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/versions")
	public ResponseEntity<?> getListAPIResource(@RequestParam(value = "apiName") String apiName) {
		LOGGER.info("Received request to get the list of versions for API with NAME: {}", apiName);
		try {
			List<Map<String, Object>> apiResources = reportUsageService.getVersions(apiName);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse.success("API versions retrieved successfully",
					apiResources);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API resources: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/years")
	public ResponseEntity<?> getListYear(Authentication authentication) {
		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		String username = Utils.isAdmin(principal.getAttributes()) ? null
				: principal.getAttributes().get("http://wso2.org/claims/username").toString();
		LOGGER.info("Received request to get the list of years");
		try {
			ApiResponse<?> response = ApiResponse.success("Years retrieved successfully",
					reportUsageService.getYears(username));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve years: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/months")
	public ResponseEntity<?> getListMonth(@RequestParam(value = "year") int year,Authentication authentication) {
		
		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		String username = Utils.isAdmin(principal.getAttributes()) ? null
				: principal.getAttributes().get("http://wso2.org/claims/username").toString();
		
		LOGGER.info("Received request to get the list of months for year: {}", year);
		try {
			ApiResponse<?> response = ApiResponse.success("Months retrieved successfully",
					reportUsageService.getMonth(username, year));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve months: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/customers")
	public ResponseEntity<?> getListCustomers(@RequestParam(value = "username", required = false) String username) {
		LOGGER.info("Received request to get the list of customers");
		try {
			ApiResponse<?> response = ApiResponse.success("List of customers retrieved successfully",
					reportUsageService.getCustomers(username));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve customers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/v2/customers")
	public ResponseEntity<?> getListCustomersv2(@RequestParam(value = "username", required = false) String username) {
		LOGGER.info("Received request to get the list of customers");
		try {
			Map<String, Object> result = new HashMap<>();
			result.put("content", reportUsageService.getCustomersv2(username));
			result.put("total", reportUsageService.getTotalCustomers(username));
			ApiResponse<?> response = ApiResponse.success("List of customers retrieved successfully", result);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve customers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	public static LocalDate parseDate(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			return LocalDate.parse(dateString, formatter);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	@GetMapping("/subscriptions/remaining")
	public ResponseEntity<?> getRemainingSubscriptions(Authentication authentication,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for remaining subscriptions.");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null
					: principal.getAttributes().get("http://wso2.org/claims/username").toString();
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Successful retrieval of remaining subscriptions.",
					reportUsageService.getSubscriptionsRemaining(username, pageable));
			LOGGER.info("Completed retrieval of remaining subscriptions.");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}

	}
}