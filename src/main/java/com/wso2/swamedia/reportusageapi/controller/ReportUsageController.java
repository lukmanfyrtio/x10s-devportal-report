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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;
import com.wso2.swamedia.reportusageapi.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Report Usage Controller", description = "APIs for reporting usage summary")
@SecurityRequirement(name = "bearerAuth")
public class ReportUsageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageController.class);

	@Autowired
	private ReportUsageService reportUsageService;

	@GetMapping("/monthly-summary")
	@Operation(summary = "Get Monthly Summary", description = "Retrieve a monthly summary of usage.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getMonthlySummary(@RequestParam(required = false) String applicationId,
			@RequestParam(required = false) String search, @RequestParam(required = false) String apiId,
			@RequestParam(required = false) String organization,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		try {
			ApiResponse<?> response = ApiResponse.success("Data retrieval successful.", reportUsageService
					.getMonthlyReport(year, month, applicationId, apiId, page, size, search, organization, keyType));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/monthly-summary/details")
	@Operation(summary = "Get Monthly Summary Details", description = "Retrieve detailed monthly summary of API data usage.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
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

			DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
					.getPrincipal();
			String username = Utils.isAdmin(principal.getAttributes()) ? null
					: principal.getAttributes().get("http://wso2.org/claims/username").toString();

			Map<String, Object> total = reportUsageService.totalMonthlyDetailLog(username, applicationId, apiId, search,
					year, month, showDeletedSubscription, keyType);
			Pageable pageable = PageRequest.of(page, size);
			LinkedHashMap<String, Object> result = new LinkedHashMap<>();
			result.put("requestCount", total.get("request_count"));
			result.put("requestOK", total.get("count_200"));
			result.put("requestNOK", total.get("count_not_200"));
			result.put("details", reportUsageService.getMonthlyDetailLogReport(username, applicationId, apiId, search,
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
	@Operation(summary = "Get Resource Summary", description = "Retrieve resource summary information.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getResourceSummary(@RequestParam(required = false) String search,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String resource,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {

		try {
			ApiResponse<?> response = ApiResponse.success("Resource summary retrieval successful.",
					reportUsageService.getResourceReport(year, month, resource, apiId, page, size, search, keyType));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resource-summary/details")
	@Operation(summary = "Get Resource Detail Log", description = "Retrieve detailed log information for a specific resource and API.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getResourceDetailLog(@RequestParam(value = "resource") String resource,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(required = false, defaultValue = "false") Boolean showDeletedSubscription,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			Authentication authentication) {

		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		String username = Utils.isAdmin(principal.getAttributes()) ? null
				: principal.getAttributes().get("http://wso2.org/claims/username").toString();

		LOGGER.info("Received request for resource detail log");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Resource detail log retrieval successful.",
					reportUsageService.getDetailLogResourceSum(username, resource, apiId, search, pageable,
							showDeletedSubscription, keyType));

			LOGGER.info("Resource detail log retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api")
	@Operation(summary = "Get Backend API Usage", description = "Retrieve information about the usage of backend APIs.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getBackendAPIUsage(@RequestParam(required = false) String apiId,
			@RequestParam(required = false) String search,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (!Utils.isAdmin()) {
			ApiResponse<Resource> response = ApiResponse.error("You do not have permission to access this API.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		LOGGER.info("Received request for backend api usage summary");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Backend api usage summary retrieval successful.",
					reportUsageService.getBackendAPIUsage(year, month, apiId, search, pageable, keyType));

			LOGGER.info("Backend api usage summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/error-summary")
	@Operation(summary = "Get Error Summary", description = "Retrieve summary information for errors.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getErrorSummary(@RequestParam(required = false) String search,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String version,
			@RequestParam(required = false, defaultValue = "false") boolean asPercent,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (!Utils.isAdmin()) {
			ApiResponse<Resource> response = ApiResponse.error("You do not have permission to access this API.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		LOGGER.info("Received request for Error summary ");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Error summary retrieval successful.",
					reportUsageService.getErrorSummary(apiId, version, asPercent, search, pageable, keyType));

			LOGGER.info("Error summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/backend-api/details")
	@Operation(summary = "Get Backend API Usage Details", description = "Retrieve detailed information for a specific backend API.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
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
	@Operation(summary = "Get List of API Names", description = "Retrieve the list of API names based on optional parameters.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListAPIName(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "organization", required = false) String organization) {
		LOGGER.info("Received request to get the list of API names");

		try {
			List<Map<String, Object>> apiNames = reportUsageService.getApis(organization);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse.success("API names retrieved successfully",
					apiNames);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API names: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/resources")
	@Operation(summary = "Get List of API Resources", description = "Retrieve the list of resources for a specific API.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListAPIResource(@RequestParam(value = "apiId") String apiId,
			Authentication authentication) {

		LOGGER.info("Received request to get the list of resources for API with ID: {}", apiId);
		try {
			List<Map<String, Object>> apiResources = reportUsageService.getApiResourceByAPI(apiId);
			ApiResponse<List<Map<String, Object>>> response = ApiResponse
					.success("API resources retrieved successfully", apiResources);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve API resources: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/versions")
	@Operation(summary = "Get List of API Versions", description = "Retrieve the list of versions for a specific API.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListAPIVersions(@RequestParam(value = "apiName") String apiName) {
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
	@Operation(summary = "Get List of Years", description = "Retrieve the list of available years for reporting.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListYear() {
		try {
			ApiResponse<?> response = ApiResponse.success("Years retrieved successfully",
					reportUsageService.getYears());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve years: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/months")
	@Operation(summary = "Get List of Months", description = "Retrieve the list of available months for reporting.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListMonth(@RequestParam(value = "year") int year, Authentication authentication) {

		LOGGER.info("Received request to get the list of months for year: {}", year);
		try {
			ApiResponse<?> response = ApiResponse.success("Months retrieved successfully",
					reportUsageService.getMonth(year));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve months: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/customers")
	@Operation(summary = "Get List of Customers", description = "Retrieve the list of available Customers for reporting.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getListCustomers() {
		LOGGER.info("Received request to get the list of customers");
		try {
			ApiResponse<?> response = ApiResponse.success("List of customers retrieved successfully",
					reportUsageService.getCustomers());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error("Failed to retrieve customers: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/v2/customers")
	@Operation(summary = "Get List of Customers V2", description = "Retrieve the list of available Customers for reporting V2.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
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
	@Operation(summary = "Get Remaining Subscriptions", description = "Retrieve information about remaining subscriptions.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Data not found", content = @Content(mediaType = "application/json")) })
	public ResponseEntity<?> getRemainingSubscriptions(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for remaining subscriptions.");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Successful retrieval of remaining subscriptions.",
					reportUsageService.getSubscriptionsRemaining(pageable));
			LOGGER.info("Completed retrieval of remaining subscriptions.");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}

	}
}