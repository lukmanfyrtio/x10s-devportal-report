package com.wso2.swamedia.reportusageapi.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;

public class DashboardAppPercentageMapper implements RowMapper<DashboardPercentageDTO> {
	@Override
	public DashboardPercentageDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		DashboardPercentageDTO apiUsageByApi = new DashboardPercentageDTO();
		apiUsageByApi.setApplicationId(rs.getString("APPLICATION_ID"));
		apiUsageByApi.setApplicationName(rs.getString("APPLICATION_NAME"));
		apiUsageByApi.setRowCount(rs.getInt("row_count"));
		apiUsageByApi.setPercentage(rs.getDouble("percentage"));
		return apiUsageByApi;
	}
}
