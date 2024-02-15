package com.wso2.swamedia.reportusageapi.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.domain.Page;

import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;
import com.wso2.swamedia.reportusageapi.dto.TotalReportDashboard;

public interface DashboardService {

	public List<?> getTopTenApiUsage(String filter, int top, String keyType) throws Exception;

	public Page<LinkedHashMap<String, Object>> getFaultOvertimeDetails(String filter, int page,
			int pageSize, String searchQuery) throws Exception;

	public List<LinkedHashMap<String, Object>> getFaultOvertime(String filter) throws Exception;

	public List<DashboardPercentageDTO> getApiUsageByApi(Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByApplication(Integer top, String keyType);

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(Integer top, String keyType);

	public TotalReportDashboard getDashboardTotalReport();

	public int getTotalAPIsByUsername();

	public int getTotalAppsByUsername();

	public int getTotalSubscriberByUsername();

	public int getTotalSubscriptionAPIByUsername();

	public int getTotalResponseFaultByUsername();

	public int getTotalUnpaidInvoicesByUsername();

	public LinkedHashMap<String, Object> getUsagePercentage(Integer top, Boolean byApplication, Boolean byResponseCode,
			Boolean byApi, String keyType);
}
