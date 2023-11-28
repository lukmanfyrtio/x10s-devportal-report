package com.wso2.swamedia.reportusageapi.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.domain.Page;

import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;
import com.wso2.swamedia.reportusageapi.dto.TotalReportDashboard;

public interface DashboardService {

	public List<?> getTopTenApiUsage(String filter, String organizationName, int top, String keyType) throws Exception;

	public Page<LinkedHashMap<String, Object>> getFaultOvertimeDetails(String filter, String organizationName, int page,
			int pageSize, String searchQuery) throws Exception;

	public List<LinkedHashMap<String, Object>> getFaultOvertime(String filter, String organizationName) throws Exception;

	public List<DashboardPercentageDTO> getApiUsageByApi(String organizationName, Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByApplication(String organizationName, Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(String organizationName, Integer top, String keyType);

	public TotalReportDashboard getDashboardTotalReport(String organizationName);

	public int getTotalAPIsByOrganizationName(String organizationName);

	public int getTotalAppsByOrganizationName(String organizationName);

	public int getTotalSubscriberByOrganizationName(String organizationName);

	public int getTotalSubscriptionAPIByOrganizationName(String organizationName);

	public int getTotalResponseFaultByOrganizationName(String organizationName);

	public int getTotalUnpaidInvoicesByOrganizationName(String organizationName);

	public LinkedHashMap<String, Object> getUsagePercentage(String getUsagePercentage, Integer top,
			Boolean byApplication, Boolean byResponseCode, Boolean byApi, String keyType);
}
