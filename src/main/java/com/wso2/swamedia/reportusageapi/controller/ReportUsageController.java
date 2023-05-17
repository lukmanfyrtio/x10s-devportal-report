package com.wso2.swamedia.reportusageapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.dto.MonthlySummary;
import com.wso2.swamedia.reportusageapi.dto.MonthlySummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummary;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummaryDetails;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;

@RestController
@RequestMapping("/report")
public class ReportUsageController {

	@Autowired
	private ReportUsageService reportUsageService;

	@GetMapping("/monthly-summary")
	public ResponseEntity<MonthlySummary> getMonthlySummary(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String application,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String username,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {
		System.out.println(authentication.getName());
		return ResponseEntity.ok(reportUsageService.getMonthlyReport(year, month, application, apiId,
				authentication.getName(), page, size, search));
	}

	@GetMapping("/monthly-summary/details")
	public ResponseEntity<Page<MonthlySummaryDetails>> getApiDataUsage(
			@RequestParam(value = "applicationId") String applicationId, @RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {
		Pageable pageable = PageRequest.of(page, size);
		Page<MonthlySummaryDetails> apiDataPage = reportUsageService.getApiDataUsage(authentication.getName(),
				applicationId, apiId, search, pageable);

		return ResponseEntity.ok(apiDataPage);
	}

	@GetMapping("/resource-summary")
	public ResponseEntity<ResourceSummary> getResourceSummary(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month, @RequestParam(required = false) String resource,
			@RequestParam(required = false) String apiId, @RequestParam(required = false) String username,
			@RequestParam(required = false) String search, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {
		System.out.println(authentication.getName());
		return ResponseEntity.ok(reportUsageService.getResourceReport(year, month, resource, apiId,
				authentication.getName(), page, size, search));
	}
	
	@GetMapping("/resource-summary/details")
	public ResponseEntity<Page<ResourceSummaryDetails>> getResourceDetailLog(
			@RequestParam(value = "resource") String resource, @RequestParam(value = "apiId") String apiId,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ResourceSummaryDetails> apiDataPage = reportUsageService.getDetailLogResourceSum(authentication.getName(), resource, apiId, search, pageable);

		return ResponseEntity.ok(apiDataPage);
	}
}
