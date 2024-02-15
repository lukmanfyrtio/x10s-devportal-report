package com.wso2.swamedia.reportusageapi.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.dto.TotalReportDashboard;
import com.wso2.swamedia.reportusageapi.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/percentage-report")
	public ResponseEntity<?> getApiUsagePercentage(
			@RequestParam(value = "top", required = false, defaultValue = "10") Integer top,
			@RequestParam(value = "byApplication", required = false, defaultValue = "false") Boolean byApplication,
			@RequestParam(value = "byApi", required = false, defaultValue = "false") Boolean byApi,
			@RequestParam(value = "byResponseCode", required = false, defaultValue = "false") Boolean byResponseCode,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType

	) {
		LOGGER.info("Received request for get percentage usage");

		try {
			ApiResponse<?> response = ApiResponse.success("Percentage usage  retrieved successfully .",
					dashboardService.getUsagePercentage(top, byApplication, byResponseCode, byApi, keyType));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}



	@GetMapping("/api-usage")
	public ResponseEntity<?> getDashboardApiUsageByDate(
			@RequestParam("filter") String filter, @RequestParam(value = "top", defaultValue = "10") int top,
			@RequestParam(value = "keyType", required = false, defaultValue = "PRODUCTION") String keyType) {
		LOGGER.info("Received request for get api usage");
		try {
			ApiResponse<?> response = ApiResponse.success("Top 10 api usage retrieved successfully .",
					dashboardService.getTopTenApiUsage(filter, top, keyType));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/api-fault")
	public ResponseEntity<?> getDashboardApiFaultByDate(
			@RequestParam("filter") String filter) {
		LOGGER.info("Received request for get api fault");
		try {
			ApiResponse<?> response = ApiResponse.success("Api fault overtime retrieved successfully .",
					dashboardService.getFaultOvertime(filter));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/api-fault/details")
	public ResponseEntity<?> getDashboardApiFaultDetails(Authentication authentication,
			@RequestParam("filter") String filter, @RequestParam(required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		LOGGER.info("Received request for get api fault details");
		try {
			ApiResponse<?> response = ApiResponse.success("Api fault overtime details retrieved successfully .",
					dashboardService.getFaultOvertimeDetails(filter, page, size, search));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/total-report")
	public ResponseEntity<?> getDashboardTotalReport(Authentication authentication) {
		LOGGER.info("Received request to retrieve the dashboard total report");
		try {
			ApiResponse<TotalReportDashboard> response = ApiResponse.success(
					"The dashboard total report has been retrieved successfully",
					dashboardService.getDashboardTotalReport());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse
					.error("Failed to retrieve the dashboard total report: " + e.getMessage());
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
}
