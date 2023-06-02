package com.wso2.swamedia.reportusageapi.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;

@RestController
@RequestMapping("/report")
public class ReportUsageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageController.class);

	@Autowired
	private ReportUsageService reportUsageService;

	@GetMapping("/monthly-summary")
	public ResponseEntity<?> getMonthlySummary(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String applicationId,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String username,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for monthly summary");
		try {

			ApiResponse<?> response = ApiResponse.success("Monthly summary retrieval successful.", reportUsageService
					.getMonthlyReport(year, month, applicationId, apiId, username, page, size, search));
			LOGGER.info("Monthly summary retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}

	}

	@GetMapping("/monthly-summary/details")
	public ResponseEntity<?> getApiDataUsage(@RequestParam(value = "applicationId") String applicationId,
			@RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(required = false) String username) {

		LOGGER.info("Received request for API Monthly detail log");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Monthly detail log retrieval successful.",
					reportUsageService.getMonthlyDetailLog(username, applicationId, apiId, search, pageable));

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
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String username,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for resource summary");
		try {
			ApiResponse<?> response = ApiResponse.success("Resource summary retrieval successful.",
					reportUsageService.getResourceReport(year, month, resource, apiId, username, page, size, search));

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
			@RequestParam(required = false) String username) {

		LOGGER.info("Received request for resource detail log");
		try {
			Pageable pageable = PageRequest.of(page, size);
			ApiResponse<?> response = ApiResponse.success("Resource detail log retrieval successful.",
					reportUsageService.getDetailLogResourceSum(username, resource, apiId, search, pageable));

			LOGGER.info("Resource detail log retrieval completed");

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
			List<Map<String, Object>> apiNames = reportUsageService.getApiNameAndId(username,organization);
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

	@GetMapping("/years")
	public ResponseEntity<?> getListYear(@RequestParam(value = "username", required = false) String username) {
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
	public ResponseEntity<?> getListMonth(@RequestParam(value = "year") int year,
			@RequestParam(value = "username", required = false) String username) {
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

	public static LocalDate parseDate(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			return LocalDate.parse(dateString, formatter);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
	
	@GetMapping("/subscriptions/remaining")
	public ResponseEntity<?> getRemainingSubscriptions(@RequestParam(required = false) String username,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		LOGGER.info("Received request for remaining subscriptions.");
		try {
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