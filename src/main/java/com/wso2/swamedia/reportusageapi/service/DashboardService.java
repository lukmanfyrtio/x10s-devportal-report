package com.wso2.swamedia.reportusageapi.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.domain.Page;

import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;
import com.wso2.swamedia.reportusageapi.dto.TotalReportDashboard;

public interface DashboardService {

	public List<?> getTopTenApiUsage(String filter, String owner, int top, String keyType) throws Exception;

	public Page<LinkedHashMap<String, Object>> getFaultOvertimeDetails(String filter, String owner, int page,
			int pageSize, String searchQuery) throws Exception;

	public List<LinkedHashMap<String, Object>> getFaultOvertime(String filter, String owner) throws Exception;

	public List<DashboardPercentageDTO> getApiUsageByApi(String username, Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByApplication(String username, Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(String username, Integer top, String keyType);

	public TotalReportDashboard getDashboardTotalReport(String username);

	public int getTotalAPIsByUsername(String username);

	public int getTotalAppsByUsername(String username);

	public int getTotalSubscriberByUsername(String username);

	public int getTotalSubscriptionAPIByUsername(String username);

	public int getTotalResponseFaultByUsername(String username);

	public int getTotalUnpaidInvoicesByUsername(String username);

	public LinkedHashMap<String, Object> getUsagePercentage(String username, Integer top, Boolean byApplication,
			Boolean byResponseCode, Boolean byApi, String keyType);
}
