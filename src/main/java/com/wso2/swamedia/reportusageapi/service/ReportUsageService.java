package com.wso2.swamedia.reportusageapi.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;
import com.wso2.swamedia.reportusageapi.dto.DataUsageApiResponse;
import com.wso2.swamedia.reportusageapi.dto.ErrorSummary;
import com.wso2.swamedia.reportusageapi.dto.MonthlySummary;
import com.wso2.swamedia.reportusageapi.dto.MonthlySummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.OrganizationDTO;
import com.wso2.swamedia.reportusageapi.dto.RequestCountDTO;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummary;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.TableRemainingDayQuota;

public interface ReportUsageService {

	public MonthlySummary getMonthlyReport(Integer year, Integer month, String applicationId, String apiId,
			String username, int page, int size, String search, String organization, Boolean showDeleted,
			String keyType) throws Exception;

	public Page<MonthlySummaryDetails> getMonthlyDetailLogReport(String organization, String applicationId, String apiId,
			String searchFilter, Pageable pageable, Integer year, Integer month, Boolean showDeletedSubscription,
			String keyType);

	public ResourceSummary getResourceReport(Integer year, Integer month, String resource, String apiId,
			String organization, int page, int size, String search, Boolean showDeletedSubscription, String keyType);

	public Page<ResourceSummaryDetails> getDetailLogResourceSum(String organization, String resource, String apiId,
			String searchFilter, Pageable pageable, Boolean showDeletedSubscription, String keyType) throws Exception;

	public List<DashboardPercentageDTO> getApiUsageByApi(LocalDate startDate, LocalDate endDate, String username);

	public List<LinkedHashMap<String, Object>> getPlanByPaymentType(Integer subsTypeId, Boolean isDeployed);

	public List<DashboardPercentageDTO> getApiUsageByApplication(LocalDate startDate, LocalDate endDate,
			String username);

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(LocalDate startDate, LocalDate endDate,
			String username);

	public Map<String, Object> getOptionalDateRangeNamedParams(LocalDate startDate, LocalDate endDate);

	public LinkedHashMap<String, Object> getUsagePercentage(LocalDate startDate, LocalDate endDate, String username);

	public List<Map<String, Object>> getApiNameAndId(String owner, String organization);

	public List<Map<String, Object>> getApis(String owner, String organization);

	public List<Map<String, Object>> getYears(String owner);

	public List<Map<String, Object>> getCustomers(String owner);

	public int getTotalCustomers(String username);

	public List<Map<String, Object>> getCustomersv2(String owner);

	public List<Map<String, Object>> getMonth(String owner, int year);

	public List<Map<String, Object>> getApiResourceByAPI(String owner, String apiId);

	public List<Map<String, Object>> getVersions(String apiName);

	public List<OrganizationDTO> getOrganizations() throws Exception;

	public Page<TableRemainingDayQuota> getSubscriptionsRemaining(String owner, Pageable pageable);

	public OrganizationDTO findOrganizationByUsername(List<OrganizationDTO> organizationDTOs, String username);

	public Page<DataUsageApiResponse> getBackendAPIUsage(String organization, Integer year, Integer month, String apiId,
			String searchFilter, Pageable pageable, String keyType);

	public Page<RequestCountDTO> getBackendAPIUsageDetails(String apiId, Pageable pageable, String keyType);

	public Page<?> getErrorSummary(String apiId, String version, boolean asPercent, String search, Pageable pageable,
			String keyType);

	public LinkedHashMap<String, Object> convertErrorSummaryToMap(ErrorSummary errorSummary);

	public Page<MonthlySummary.ApiDetails> getMonthlyTotalRowByGroupByWithSearchAndPageable(String owner, Integer year,
			Integer month, String apiId, Boolean showDeleted, String applicationId, String search, String organization,
			Pageable pageable, String keyType);

	public Map<String, Object> getTotalApisAndRequestsByOwnerAndFilters(String owner, Integer year, Integer month,
			String apiId, String applicationId, String organization, Boolean showDeleted, String keyType);

	public Map<String, Object> totalMonthlyDetailLog(String organization, String applicationId, String apiId,
			String searchFilter, Integer year, Integer month, Boolean showDeleted, String keyType);

	public Page<MonthlySummaryDetails> fetchMonthlyDetailLogData(Pageable pageable, String owner, String applicationId,
			String apiId, String searchFilter, Integer year, Integer month, Boolean showDeleted, String keyType);

	public Map<String, Object> getResourceSumTotalData(String owner, Integer year, Integer month, String apiId,
			String resource, Boolean showDeleted, String keyType);

	public Page<ResourceSummary.ApiDetails> getResourceSumListData(String owner, Integer year, Integer month,
			String apiId, String resource, String search, Pageable pageable, Boolean showDeleted, String keyType);

	public Page<ResourceSummaryDetails> getDetailLogResourceSum(Pageable pageable, String owner, String resource,
			String apiId, String searchFilter, Boolean showDeleted, String keyType);

	public String buildCountQuery(String originalQuery);
}
