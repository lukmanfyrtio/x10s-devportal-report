package com.wso2.swamedia.reportusageapi.service;

import java.math.BigInteger;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wso2.swamedia.reportusageapi.dto.MonthlySummary;
import com.wso2.swamedia.reportusageapi.dto.MonthlySummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummary;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummaryDetails;
import com.wso2.swamedia.reportusageapi.repo.DataUsageApiRepository;

@Service
public class ReportUsageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageService.class);

	@Autowired
	private DataUsageApiRepository dataUsageApiRepository;

	public MonthlySummary getMonthlyReport(Integer year, Integer month, String applicationId, String apiId,
			String username, int page, int size, String search) throws Exception {
		LOGGER.info("Retrieving monthly report for year: {}, month: {}, username: {}", year, month, username);

		MonthlySummary monthlySummary = new MonthlySummary();
		try {
			Map<String, Object> dataTotal = dataUsageApiRepository.getTotalApisAndRequestsByOwnerAndFilters(username,
					year, month, apiId, applicationId);
			monthlySummary.setTotalApis(Integer.valueOf(dataTotal.get("total_apis").toString()));
			monthlySummary.setRequestCount(Integer.valueOf(dataTotal.get("total_request").toString()));
		} catch (Exception e) {
			String error = String.format("Error retrieving total APIs and requests: {}", e.getMessage());
			LOGGER.error(error);
			// Handle the exception or throw a custom exception
			throw new Exception(e.getMessage());
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Object[]> result = dataUsageApiRepository.getMonthlyTotalRowByGroupByWithSearchAndPageable(username,
					year, month, apiId, applicationId, search, pageable);

			Page<MonthlySummary.ApiDetails> pageM = result.map(row -> {
				return new MonthlySummary.ApiDetails((String) row[0], (String) row[3], (String) row[1], (String) row[4],
						(String) row[2], (BigInteger) row[5], (String) row[6]);
			});
			monthlySummary.setDetails(pageM);
		} catch (Exception e) {
			// Handle the exception or throw a custom exception

			String error = String.format("Error retrieving monthly report details: {}", e.getMessage());
			LOGGER.error(error);
			// Handle the exception or throw a custom exception
			throw new Exception(e.getMessage());
		}

		LOGGER.info("Monthly report retrieval completed");

		return monthlySummary;
	}

	public Page<MonthlySummaryDetails> getMonthlyDetailLog(String owner, String applicationId, String apiId,
			String searchFilter, Pageable pageable) throws Exception {
		LOGGER.info("Retrieving API Monthly detail log report for owner: {}, applicationId: {}, apiId: {}", owner,
				applicationId, apiId);

		Page<MonthlySummaryDetails> pageM = null;
		try {
			Page<Object[]> result = dataUsageApiRepository.getMonthlyDetailLog(pageable, owner, applicationId, apiId,
					searchFilter);

			pageM = result.map(row -> {
				String requestTimestamp = (String) row[0];
				String resource = (String) row[1];
				Integer proxyResponseCode = (Integer) row[2];
				String apiIdRes = (String) row[3];
				String applicationIdres = (String) row[4];
				String apiNameQ = (String) row[5];
				String appNameQ = (String) row[6];

				return new MonthlySummaryDetails(requestTimestamp, resource, proxyResponseCode, apiIdRes,
						applicationIdres, apiNameQ, appNameQ);
			});
		} catch (Exception e) {
			String error = String.format("Error retrieving API monthly detail log report: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		LOGGER.info("API Monthly detail log report retrieval completed");

		return pageM;
	}

	public ResourceSummary getResourceReport(Integer year, Integer month, String resource, String apiId,
			String username, int page, int size, String search) throws Exception {
		LOGGER.info("Retrieving resource summary for year: {}, month: {}, resource: {}, username: {}", year, month,
				resource, username);

		ResourceSummary resourceSummary = new ResourceSummary();
		try {
			Map<String, Object> dataTotal = dataUsageApiRepository.getResourceSumTotal(username, year, month, apiId,
					resource);
			resourceSummary.setTotalApis(Integer.valueOf(dataTotal.get("total_apis").toString()));
			resourceSummary.setRequestCount(Integer.valueOf(dataTotal.get("total_request").toString()));
		} catch (Exception e) {
			String error = String.format("Error retrieving total APIs and requests for resource: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Object[]> result = dataUsageApiRepository.getResourceSumList(username, year, month, apiId, resource,
					search, pageable);

			Page<ResourceSummary.ApiDetails> pageM = result.map(row -> {
				String apiNameQ = (String) row[0];
				String apiVersionQ = (String) row[1];
				String resourceQ = (String) row[2];
				String apiMethodQ = (String) row[3];
				BigInteger count = (BigInteger) row[4];
				String apiIdQ = (String) row[5];
				return new ResourceSummary.ApiDetails(apiNameQ, apiVersionQ, resourceQ, apiMethodQ, count, apiIdQ);
			});
			resourceSummary.setDetails(pageM);
		} catch (Exception e) {
			String error = String.format("Error retrieving resource summary details: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		LOGGER.info("Resource summary retrieval completed");

		return resourceSummary;
	}

	public Page<ResourceSummaryDetails> getDetailLogResourceSum(String owner, String resource, String apiId,
			String searchFilter, Pageable pageable) throws Exception {
		LOGGER.info("Retrieving resource detail log for owner: {}, resource: {}, apiId: {}", owner, resource, apiId);

		Page<ResourceSummaryDetails> pageM = null;
		try {
			Page<Object[]> result = dataUsageApiRepository.getDetailLogResourceSum(pageable, owner, resource, apiId,
					searchFilter);

			pageM = result.map(row -> {
				String appName = (String) row[0];
				BigInteger requestCount = (BigInteger) row[2];
				BigInteger countNOK = (BigInteger) row[3];
				BigInteger countOK = (BigInteger) row[4];

				return new ResourceSummaryDetails(appName, requestCount, countOK, countNOK, apiId);
			});
		} catch (Exception e) {
			String error = String.format("Error retrieving resource detail log: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		LOGGER.info("Resource detail log retrieval completed");

		return pageM;
	}
}
