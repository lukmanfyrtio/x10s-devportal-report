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

import com.wso2.swamedia.reportusageapi.Utils;
import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;

@RestController
@RequestMapping("/v1/report")
public class ReportUsageControllerv2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageControllerv2.class);

	@Autowired
	private ReportUsageService reportUsageService;

	@GetMapping("/monthly-summary")
	public ResponseEntity<?> getMonthlySummaryV1(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String applicationId,
			Authentication authentication, @RequestParam(value = "apiId",required = false) String apiId,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "showDeletedSubscription", defaultValue = "false") Boolean showDeletedSubscription) {

		LOGGER.info("Received request for monthly summary");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();
			String organization = Utils.isAdmin(principal.getAttributes()) ? null
					: principal.getAttributes().get("http://wso2.org/claims/organization").toString();

			ApiResponse<?> response = ApiResponse.success("Monthly summary retrieval successful.", reportUsageService
					.getMonthlyReport(year, month, applicationId, apiId, username, page, size, search, organization,showDeletedSubscription));
			LOGGER.info("Monthly summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}

	}

	@GetMapping("/monthly-summary/details")
	public ResponseEntity<?> getApiDataUsageV1(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(value = "applicationId") String applicationId,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "showDeletedSubscription", defaultValue = "false") Boolean showDeletedSubscription,
			Authentication authentication) {

		LOGGER.info("Received request for API Monthly detail log");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			Map<String, Object> total = reportUsageService.totalMonthlyDetailLog(username, applicationId, apiId, search,
					year, month,showDeletedSubscription);
			Pageable pageable = PageRequest.of(page, size);
			LinkedHashMap<String, Object> result = new LinkedHashMap<>();
			result.put("requestCount", total.get("request_count"));
			result.put("requestOK", total.get("count_200"));
			result.put("requestNOK", total.get("count_not_200"));
			result.put("details", reportUsageService.getMonthlyDetailLog(username, applicationId, apiId, search,
					pageable, year, month,showDeletedSubscription));

			ApiResponse<?> response = ApiResponse.success("Monthly detail log retrieval successful.", result);

			LOGGER.info("API Monthly detail log retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resource-summary")
	public ResponseEntity<?> getResourceSummaryV1(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String resource,
			@RequestParam(required = false) String apiId, Authentication authentication,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "showDeletedSubscription", defaultValue = "false") Boolean showDeletedSubscription) {

		LOGGER.info("Received request for resource summary");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			ApiResponse<?> response = ApiResponse.success("Resource summary retrieval successful.",
					reportUsageService.getResourceReport(year, month, resource, apiId, username, page, size, search,showDeletedSubscription));

			LOGGER.info("Resource summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
		
	}

	@GetMapping("/resource-summary/details")
	public ResponseEntity<?> getResourceDetailLogV1(@RequestParam(value = "resource") String resource,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "showDeletedSubscription", defaultValue = "false") Boolean showDeletedSubscription,
			Authentication authentication) {

		LOGGER.info("Received request for resource detail log");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Resource detail log retrieval successful.",
					reportUsageService.getDetailLogResourceSum(username, resource, apiId, search, pageable,showDeletedSubscription));

			LOGGER.info("Resource detail log retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api")
	public ResponseEntity<?> getBackendAPIUsageV1(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String apiId,
			Authentication authentication, @RequestParam(required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for backend api usage summary");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Backend api usage summary retrieval successful.",
					reportUsageService.getBackendAPIUsage(username, year, month, apiId, search, pageable));

			LOGGER.info("Backend api usage summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/error-summary")
	public ResponseEntity<?> getErrorSummaryV1(@RequestParam(required = false) String apiId,
			@RequestParam(required = false) String version, @RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "false") boolean asPercent,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {

		LOGGER.info("Received request for Error summary ");
		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		if (Utils.isAdmin(principal.getAttributes())) {
			ApiResponse<?> response = ApiResponse.success("Only admins can access this functionality.", null);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Error summary retrieval successful.",
					reportUsageService.getErrorSummary(apiId, version, asPercent, search, pageable));

			LOGGER.info("Error summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api/details")
	public ResponseEntity<?> getBackendAPIUsageDetailsV1(Authentication authentication,
			@RequestParam(required = false) String apiId, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		if (Utils.isAdmin(principal.getAttributes())) {
			ApiResponse<?> response = ApiResponse.success("Only admins can access this functionality.", null);
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
					reportUsageService.getBackendAPIUsageDetails(apiId, pageable));

			LOGGER.info("Backend api usage summary details retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/apis")
	public ResponseEntity<?> getListAPINameV1(Authentication authentication) {
		LOGGER.info("Received request to get the list of API names");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();
			String organization = Utils.isAdmin(principal.getAttributes()) ? null
					: principal.getAttributes().get("http://wso2.org/claims/organization").toString();

			List<Map<String, Object>> apiNames = reportUsageService.getApiNameAndId(username, organization);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse.success("API names retrieved successfully",
					apiNames);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API names: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resources")
	public ResponseEntity<?> getListAPIResourceV1(@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "username", required = false) String username) {
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
	public ResponseEntity<?> getListAPIResourceV1(@RequestParam(value = "apiName") String apiName) {
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
	public ResponseEntity<?> getListYearV1(Authentication authentication) {
		LOGGER.info("Received request to get the list of years");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			ApiResponse<?> response = ApiResponse.success("Years retrieved successfully",
					reportUsageService.getYears(username));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve years: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/months")
	public ResponseEntity<?> getListMonthV1(@RequestParam(value = "year") int year, Authentication authentication) {
		LOGGER.info("Received request to get the list of months for year: {}", year);
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			ApiResponse<?> response = ApiResponse.success("Months retrieved successfully",
					reportUsageService.getMonth(username, year));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve months: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/customers")
	public ResponseEntity<?> getListCustomersV1(Authentication authentication) {
		LOGGER.info("Received request to get the list of customers");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

			ApiResponse<?> response = ApiResponse.success("List of customers retrieved successfully",
					reportUsageService.getCustomers(username));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve customers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/v2/customers")
	public ResponseEntity<?> getListCustomersv2V1(Authentication authentication) {
		LOGGER.info("Received request to get the list of customers");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

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
	public ResponseEntity<?> getRemainingSubscriptionsV1(Authentication authentication,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for remaining subscriptions.");
		try {
			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null : principal.getAttributes().get("http://wso2.org/claims/username").toString();

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