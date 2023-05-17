package com.wso2.swamedia.reportusageapi.service;

import java.math.BigInteger;
import java.util.Map;

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

	@Autowired
	private DataUsageApiRepository dataUsageApiRepository;

	public MonthlySummary getMonthlyReport(Integer year, Integer month, String application, String apiId,
			String username, int page, int size, String search) {
		MonthlySummary monthlySummary = new MonthlySummary();
		Map<String, Object> dataTotal = dataUsageApiRepository.getTotalApisAndRequestsByOwnerAndFilters(username, year,
				month, apiId, application);
		monthlySummary.setTotalApis(Integer.valueOf(dataTotal.get("total_apis").toString()));
		monthlySummary.setRequestCount(Integer.valueOf(dataTotal.get("total_request").toString()));

		Pageable pageable = PageRequest.of(page, size);
		Page<Object[]> result = dataUsageApiRepository.getMonthlyTotalRowByGroupByWithSearchAndPageable(username, year,
				month, apiId, application, search, pageable);

		Page<MonthlySummary.ApiDetails> pageM = result.map(row -> {
			return new MonthlySummary.ApiDetails((String) row[0], (String) row[3], (String) row[1], (String) row[4],
					(String) row[2], (BigInteger) row[5], (String) row[6]);
		});
		monthlySummary.setDetails(pageM);
		return monthlySummary;
	}

	public Page<MonthlySummaryDetails> getApiDataUsage(String owner, String applicationId, String apiId,
			String searchFilter, Pageable pageable) {
		Page<Object[]> result = dataUsageApiRepository.getDetailApplicationUsage(pageable, owner, applicationId, apiId,
				searchFilter);
		return result.map(row -> {
			System.out.println((String) row[0]);
			String requestTimestamp = (String) row[0];
			String resource = (String) row[1];
			Integer proxyResponseCode = (Integer) row[2];
			String apiIdRes = (String) row[3];
			String applicationIdres = (String) row[4];

			return new MonthlySummaryDetails(requestTimestamp, resource, proxyResponseCode, apiIdRes, applicationIdres);
		});
	}

	public ResourceSummary getResourceReport(Integer year, Integer month, String resource, String apiId,
			String username, int page, int size, String search) {
		ResourceSummary resourceSummary = new ResourceSummary();
		Map<String, Object> dataTotal = dataUsageApiRepository.getResourceSumTotal(username, year, month, apiId,
				resource);
		resourceSummary.setTotalApis(Integer.valueOf(dataTotal.get("total_apis").toString()));
		resourceSummary.setRequestCount(Integer.valueOf(dataTotal.get("total_request").toString()));

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
		return resourceSummary;
	}
	
	public Page<ResourceSummaryDetails> getDetailLogResourceSum(String owner, String resource, String apiId,
			String searchFilter, Pageable pageable) {
		Page<Object[]> result = dataUsageApiRepository.getDetailLogResourceSum(pageable, owner, resource, apiId,
				searchFilter);
		return result.map(row -> {
			String appName = (String) row[0];
//			String apiName = (String) row[1];
			BigInteger requestCount = (BigInteger) row[2];
			BigInteger countNOK = (BigInteger) row[3];
			BigInteger countOK = (BigInteger) row[4];
//			String apiIdQ = (String) row[5];
			return new ResourceSummaryDetails(appName, requestCount, countOK, countNOK, apiId);
		});
	}
}
