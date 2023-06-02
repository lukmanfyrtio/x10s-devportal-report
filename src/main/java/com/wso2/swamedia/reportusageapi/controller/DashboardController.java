package com.wso2.swamedia.reportusageapi.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<?> getApiUsagePercentage(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "periodStartDate", required = false) String periodStartDate,
			@RequestParam(value = "periodEndDate", required = false) String periodEndDate) {
		LOGGER.info("Received request for get percentage usage");

		LocalDate pStartDate = parseDate(periodStartDate);
		LocalDate pEndDate = parseDate(periodEndDate);

		if (pStartDate == null || pEndDate == null) {
			ApiResponse<?> response = ApiResponse
					.error("Invalid date format. The date should be in the format yyyy-MM-dd.");
			return ResponseEntity.badRequest().body(response);
		}

		try {
			ApiResponse<?> response = ApiResponse.success("Percentage usage  retrieved successfully .",
					dashboardService.getUsagePercentage(pStartDate, pEndDate, username));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/quota-report")
	public ResponseEntity<?> getQuotaReport() {
		LOGGER.info("Received request for get quota report");
		try {
			return ResponseEntity.ok(null);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/api-usage")
	public ResponseEntity<?> getDashboardApiUsageByDate(
			@RequestParam(value = "username", required = false) String username, @RequestParam("filter") String filter,
			@RequestParam(value = "top", defaultValue = "10") int top) {
		LOGGER.info("Received request for get api usage");
		try {
			ApiResponse<?> response = ApiResponse.success("Top 10 api usage retrieved successfully .",
					dashboardService.getTopTenApiUsage(filter, username, top));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/api-fault")
	public ResponseEntity<?> getDashboardApiFaultByDate(
			@RequestParam(value = "username", required = false) String username,
			@RequestParam("filter") String filter) {
		LOGGER.info("Received request for get api fault");
		try {
			ApiResponse<?> response = ApiResponse.success("Api fault overtime retrieved successfully .",
					dashboardService.getFaultOvertime(filter, username));

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	@GetMapping("/total-report")
	public ResponseEntity<?> getDashboardTotalReport(
			@RequestParam(value = "username", required = false) String username) {
		LOGGER.info("Received request to retrieve the dashboard total report");
		try {

			ApiResponse<TotalReportDashboard> response = ApiResponse.success("The dashboard total report has been retrieved successfully",
					dashboardService.getDashboardTotalReport(username));
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
