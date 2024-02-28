package com.wso2.swamedia.reportusageapi.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.wso2.swamedia.reportusageapi.dto.ChartDTO;
import com.wso2.swamedia.reportusageapi.dto.DashboardPercentageDTO;
import com.wso2.swamedia.reportusageapi.dto.TotalReportDashboard;
import com.wso2.swamedia.reportusageapi.mapper.DashboardApiPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardAppPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardResCodePercentageMapper;
import com.wso2.swamedia.reportusageapi.service.DashboardService;
import com.wso2.swamedia.reportusageapi.utils.DBUtilsBilling;
import com.wso2.swamedia.reportusageapi.utils.DBUtilsUser;
import com.wso2.swamedia.reportusageapi.utils.Utils;
@Service
@Profile("mysql")
public class DashboardServiceImpl implements DashboardService{

	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DBUtilsBilling dbUtilsBilling;
	
	@Autowired
	private DBUtilsUser dbUtilsUser;

	public List<?> getTopTenApiUsage(String filter, int top, String keyType) throws Exception {
		String query = "";
		List<LinkedHashMap<String, Object>> finalResult = new ArrayList<>();
		switch (filter) {
		case "today":
			query = "SELECT 'today' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d %H') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "INNER JOIN (SELECT API_NAME, COUNT(*) AS Usage_Count " 
					+ "            FROM DATA_USAGE_API "
					+ "            WHERE DATE(REQUEST_TIMESTAMP) = CURDATE() " 
					+ "            GROUP BY API_NAME "
					+ "            ORDER BY Usage_Count DESC "
					+ "            LIMIT :top) top_10 ON du.API_NAME = top_10.API_NAME "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ " AND DATE(du.REQUEST_TIMESTAMP) = CURDATE() " 
					+ " AND du.KEY_TYPE = :keyType "
					+ " AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		case "week":
			query = "SELECT 'week' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "INNER JOIN (SELECT API_NAME, COUNT(*) AS Usage_Count " 
					+ "            FROM DATA_USAGE_API WHERE"
					+ "			   YEARWEEK(REQUEST_TIMESTAMP) = YEARWEEK(CURDATE()) "
					+ "            GROUP BY API_NAME " 
					+ "            ORDER BY Usage_Count DESC "
					+ "            LIMIT :top) top_10 ON du.API_NAME = top_10.API_NAME "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ " AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ " AND YEARWEEK(du.REQUEST_TIMESTAMP) = YEARWEEK(CURDATE()) "
					+ " AND du.KEY_TYPE = :keyType "
					+ " AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
		case "month":
			query = "SELECT 'month' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "INNER JOIN (SELECT API_NAME, COUNT(*) AS Usage_Count " + "            FROM DATA_USAGE_API WHERE "
					+ "			   YEAR(REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ "			   AND MONTH(REQUEST_TIMESTAMP) = MONTH(CURDATE()) " + "            GROUP BY API_NAME "
					+ "            ORDER BY Usage_Count DESC "
					+ "            LIMIT :top) top_10 ON du.API_NAME = top_10.API_NAME "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ " AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ " AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ " AND MONTH(du.REQUEST_TIMESTAMP) = MONTH(CURDATE()) "
					+ " AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') " 
					+ " AND du.KEY_TYPE = :keyType "
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		case "year":
			query = "SELECT 'year' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m') AS intervalData,"
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "INNER JOIN (SELECT API_NAME, COUNT(*) AS Usage_Count " 
					+ "            FROM DATA_USAGE_API "
					+ "            WHERE " 
					+ "			   YEAR(REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ "            GROUP BY API_NAME " 
					+ "            ORDER BY Usage_Count DESC "
					+ "            LIMIT :top) top_10 ON du.API_NAME = top_10.API_NAME "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) AND "
					+ " (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ " AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ " AND du.KEY_TYPE = :keyType "
					+ " AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		default:
			throw new Exception("Invalid filter provided. Valid filters: today, week, month, year");
		}


		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("isAdmin", Utils.isAdmin());
		parameters.addValue("username", Utils.getUsernameWithTenantDomain());
		parameters.addValue("tenantDomain", Utils.getTenantFromUsername());
		parameters.addValue("top", top);
		parameters.addValue("keyType", keyType);
		LOGGER.info(query);

		List<ChartDTO> queryResult = namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			ChartDTO data = new ChartDTO();
			LocalDate dateTime = null;
			if (rs.getString("type").equalsIgnoreCase("today")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
				LocalDateTime dateTime1 = LocalDateTime.parse(rs.getString("intervalData"), formatter);
				data.setXHour(dateTime1);
			} else if (rs.getString("type").equalsIgnoreCase("week")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
			} else if (rs.getString("type").equalsIgnoreCase("month")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
				YearMonth yearMonth = YearMonth.parse(rs.getString("intervalData"), formatter);
				dateTime = yearMonth.atDay(1);
			}
			String apiName = rs.getString("API_NAME");
			int totalUsage = rs.getInt("total_usage");
			data.setX(dateTime);
			data.setTotalUsage(totalUsage);
			data.setApiName(apiName);

			return data;
		});

		// ...

		// Group the queryResult by API Name
		Map<String, List<ChartDTO>> apiDataMaps = queryResult.stream()
				.collect(Collectors.groupingBy(ChartDTO::getApiName));

		// Iterate over each API Name and its corresponding data
		for (Map.Entry<String, List<ChartDTO>> entry : apiDataMaps.entrySet()) {
			String apiName = entry.getKey();
			List<ChartDTO> apiData = entry.getValue();

			// Create a new data map for the API Name
			LinkedHashMap<String, Object> apiDataMap = new LinkedHashMap<>();
			apiDataMap.put("apiName", apiName);

			// Create a list to store the data for each date
			List<LinkedHashMap<String, Object>> apiDateData = new ArrayList<>();

			if (filter.equals("week")) {
				LocalDate currentDate = LocalDate.now();
				LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

				for (int i = 0; i < 7; i++) {
					LocalDate date = startOfWeek.plusDays(i);

					// Find the ChartDTO object for the current date
					ChartDTO chartDTO = apiData.stream()
							.filter(data -> data.getX().getDayOfMonth() == date.getDayOfMonth()).findFirst()
							.orElse(null);

					// Create a data map for the date and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", date);
					data.put("totalUsage", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("month")) {
				LocalDate currentDate = LocalDate.now();
				int daysInMonth = currentDate.lengthOfMonth();

				for (int i = 1; i <= daysInMonth; i++) {
					LocalDate dateI = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), i);

					// Find the ChartDTO object for the current date
					ChartDTO chartDTO = apiData.stream().filter(data -> data.getX().equals(dateI)).findFirst()
							.orElse(null);

					// Create a data map for the date and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", dateI);
					data.put("totalUsage", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("year")) {
				LocalDate currentDate = LocalDate.now();

				for (int i = 1; i <= 12; i++) {
					YearMonth yearMonth = YearMonth.of(currentDate.getYear(), i);

					// Find the ChartDTO object for the current year-month
					ChartDTO chartDTO = apiData.stream().filter(data -> YearMonth.from(data.getX()).equals(yearMonth))
							.findFirst().orElse(null);

					// Create a data map for the year-month and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
					data.put("totalUsage", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("today")) {
				LocalDate currentDate = LocalDate.now();

				for (int i = 0; i < 24; i++) {
					LocalDateTime dateTime = LocalDateTime.of(currentDate, LocalTime.of(i, 0));

					// Find the ChartDTO object for the current hour
					ChartDTO chartDTO = apiData.stream()
							.filter(data -> data.getXHour() != null && data.getXHour().equals(dateTime)).findFirst()
							.orElse(null);

					// Create a data map for the hour and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
					data.put("x", dateTime.format(formatter));
					data.put("totalUsage", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			}

			// Add the date data list to the API Name data map
			apiDataMap.put("data", apiDateData);

			// Add the API Name data map to the finalResult
			finalResult.add(apiDataMap);
		}

		return finalResult;

	}
	

	public Page<LinkedHashMap<String, Object>> getFaultOvertimeDetails(String filter, int page, int pageSize, String searchQuery) throws Exception {
	    String countQuery = "";
	    String query = "";
	    
	    switch (filter) {
	        case "today":
	            countQuery = "SELECT COUNT(DISTINCT API_NAME,APPLICATION_NAME,du.APPLICATION_OWNER) FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND DATE(du.REQUEST_TIMESTAMP) = CURDATE() ";
	            query = "SELECT attr.UM_ATTR_VALUE ,du.APPLICATION_OWNER,'today' as type, DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d %H') AS intervalData, "
	                    + "du.API_NAME, du.APPLICATION_NAME, COUNT(*) AS total_usage "
	                    + "FROM DATA_USAGE_API du "
	                    + "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND DATE(du.REQUEST_TIMESTAMP) = CURDATE() ";
	            break;
	        case "week":
	            countQuery = "SELECT COUNT(DISTINCT API_NAME,APPLICATION_NAME,du.APPLICATION_OWNER) FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
	                    + "AND YEARWEEK(du.REQUEST_TIMESTAMP) = YEARWEEK(CURDATE()) ";
	            query = "SELECT attr.UM_ATTR_VALUE ,du.APPLICATION_OWNER,'week' as type, DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
	                    + "du.API_NAME, du.APPLICATION_NAME, COUNT(*) AS total_usage "
	                    + "FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
	                    + "AND YEARWEEK(du.REQUEST_TIMESTAMP) = YEARWEEK(CURDATE()) ";
	            break;
	        case "month":
	            countQuery = "SELECT COUNT(DISTINCT API_NAME,APPLICATION_NAME,du.APPLICATION_OWNER) FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
	                    + "AND MONTH(du.REQUEST_TIMESTAMP) = MONTH(CURDATE()) ";
	            query = "SELECT attr.UM_ATTR_VALUE ,du.APPLICATION_OWNER,'month' as type, DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
	                    + "du.API_NAME, du.APPLICATION_NAME, COUNT(*) AS total_usage "
	                    + "FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
	                    + "AND MONTH(du.REQUEST_TIMESTAMP) = MONTH(CURDATE()) ";
	            break;
	        case "year":
	            countQuery = "SELECT COUNT(DISTINCT API_NAME,APPLICATION_NAME,du.APPLICATION_OWNER) FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) ";
	            query = "SELECT attr.UM_ATTR_VALUE ,du.APPLICATION_OWNER, 'year' as type, DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m') AS intervalData, "
	                    + "du.API_NAME, du.APPLICATION_NAME, COUNT(*) AS total_usage "
	                    + "FROM DATA_USAGE_API du "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER uu ON du.APPLICATION_OWNER = uu.UM_USER_NAME "
	        			+ "LEFT JOIN "+dbUtilsUser.getSchemaName()+".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
	        			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
						+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
						+ "s.subscription_id = du.SUBSCRIPTION_UUID "
						+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
						+ "AND (:isAdmin = true  OR s.is_active = true) "
						+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
	                    + "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
	                    + "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
	                    + "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) ";
	            break;
	        default:
	            throw new Exception("Invalid filter provided. Valid filters: today, week, month, year");
	    }

	    // Add search condition if provided
	    if (searchQuery != null && !searchQuery.isEmpty()) {
	        countQuery += "AND (du.API_NAME LIKE CONCAT('%', :searchQuery, '%') OR du.APPLICATION_NAME LIKE CONCAT('%', :searchQuery, '%')) ";
	        query += "AND (du.API_NAME LIKE CONCAT('%', :searchQuery, '%') OR du.APPLICATION_NAME LIKE CONCAT('%', :searchQuery, '%')) ";
	    }
	    query += "GROUP BY intervalData, du.API_NAME, du.APPLICATION_NAME,attr.UM_ATTR_VALUE ,du.APPLICATION_OWNER "
	            + "ORDER BY intervalData DESC, total_usage DESC ";

	    // Count total records per filter
	    MapSqlParameterSource countParams = new MapSqlParameterSource();
	    countParams.addValue("isAdmin", Utils.isAdmin());
	    countParams.addValue("username", Utils.getUsernameWithTenantDomain());
	    countParams.addValue("tenantDomain", Utils.getTenantFromUsername());
	    countParams.addValue("searchQuery", searchQuery);
	    int totalRecords = namedParameterJdbcTemplate.queryForObject(countQuery, countParams, Integer.class);

	    // Fetch paginated data
	    Pageable pageable = PageRequest.of(page, pageSize);
	    query += "LIMIT :pageSize OFFSET :offset";
	    MapSqlParameterSource queryParams = new MapSqlParameterSource();
	    queryParams.addValue("isAdmin", Utils.isAdmin());
	    queryParams.addValue("username", Utils.getUsernameWithTenantDomain());
	    queryParams.addValue("tenantDomain", Utils.getTenantFromUsername());
	    queryParams.addValue("searchQuery", searchQuery);
	    queryParams.addValue("pageSize", pageable.getPageSize());
	    queryParams.addValue("offset", pageable.getOffset());

	    List<LinkedHashMap<String, Object>> queryResult = namedParameterJdbcTemplate.query(query, queryParams, (rs, rowNum) -> {
			LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
			LocalDate dateTime = null;
			if (rs.getString("type").equalsIgnoreCase("today")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
				LocalDateTime dateTime1 = LocalDateTime.parse(rs.getString("intervalData"), formatter);
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
				data.put("time", dateTime1.format(formatter2));
			} else if (rs.getString("type").equalsIgnoreCase("week")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
				data.put("time", dateTime);
			} else if (rs.getString("type").equalsIgnoreCase("month")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
				data.put("time", dateTime);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
				YearMonth yearMonth = YearMonth.parse(rs.getString("intervalData"), formatter);
				dateTime = yearMonth.atDay(1);
				data.put("time",yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
			}
			String apiName = rs.getString("API_NAME");
			int totalUsage = rs.getInt("total_usage");
			data.put("apiName", apiName);
			data.put("applicationName", rs.getString("APPLICATION_NAME"));
			data.put("applicationOwner", rs.getString("APPLICATION_OWNER"));
			data.put("organization", rs.getString("UM_ATTR_VALUE"));
			data.put("totalUsage", totalUsage);

			return data;
		});
	    
	    Page<LinkedHashMap<String, Object>> pageResult = new PageImpl<>(queryResult, pageable, totalRecords);

	    return pageResult;
	}


	public List<LinkedHashMap<String, Object>> getFaultOvertime(String filter) throws Exception {
		String query = "";
		List<LinkedHashMap<String, Object>> finalResult = new ArrayList<>();
		switch (filter) {
		case "today":
			query = "SELECT 'today' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d %H') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') " 
					+ "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
					+ "AND DATE(du.REQUEST_TIMESTAMP) = CURDATE() " 
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		case "week":
			query = "SELECT 'week' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') " 
					+ "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
					+ "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ "AND YEARWEEK(du.REQUEST_TIMESTAMP) = YEARWEEK(CURDATE()) "
					+ "GROUP BY intervalData, du.API_NAME " + "ORDER BY intervalData DESC, total_usage DESC ";
		case "month":
			query = "SELECT 'month' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m-%d') AS intervalData, "
					+ "du.API_NAME, COUNT(*) AS total_usage " 
					+ "FROM DATA_USAGE_API du "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') " 
					+ "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
					+ "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) "
					+ "AND MONTH(du.REQUEST_TIMESTAMP) = MONTH(CURDATE()) " 
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		case "year":
			query = "SELECT 'year' as type,DATE_FORMAT(du.REQUEST_TIMESTAMP, '%Y-%m') AS intervalData,"
					+ "du.API_NAME, COUNT(*) AS total_usage " + "FROM DATA_USAGE_API du "
					+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on "
					+ "s.subscription_id = du.SUBSCRIPTION_UUID "
					+ "WHERE (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
					+ "AND (:isAdmin = true  OR du.APPLICATION_OWNER = :username) "
					+ "AND du.APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') " 
					+ "AND NOT (du.PROXY_RESPONSE_CODE BETWEEN 200 AND 299) "
					+ "AND YEAR(du.REQUEST_TIMESTAMP) = YEAR(CURDATE()) " 
					+ "GROUP BY intervalData, du.API_NAME "
					+ "ORDER BY intervalData DESC, total_usage DESC ";
			break;

		default:
			throw new Exception("Invalid filter provided. Valid filters: today, week, month, year");
		}

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("isAdmin", Utils.isAdmin());
		parameters.addValue("username", Utils.getUsernameWithTenantDomain());
		parameters.addValue("tenantDomain", Utils.getTenantFromUsername());
		LOGGER.info(query);

		List<ChartDTO> queryResult = namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			ChartDTO data = new ChartDTO();
			LocalDate dateTime = null;
			if (rs.getString("type").equalsIgnoreCase("today")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
				LocalDateTime dateTime1 = LocalDateTime.parse(rs.getString("intervalData"), formatter);
				data.setXHour(dateTime1);
			} else if (rs.getString("type").equalsIgnoreCase("week")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
			} else if (rs.getString("type").equalsIgnoreCase("month")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dateTime = LocalDate.parse(rs.getString("intervalData"), formatter);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
				YearMonth yearMonth = YearMonth.parse(rs.getString("intervalData"), formatter);
				dateTime = yearMonth.atDay(1);
			}
			String apiName = rs.getString("API_NAME");
			int totalUsage = rs.getInt("total_usage");
			data.setX(dateTime);
			data.setTotalUsage(totalUsage);
			data.setApiName(apiName);

			return data;
		});

		// ...

		// Group the queryResult by API Name
		Map<String, List<ChartDTO>> apiDataMaps = queryResult.stream()
				.collect(Collectors.groupingBy(ChartDTO::getApiName));

		// Iterate over each API Name and its corresponding data
		for (Map.Entry<String, List<ChartDTO>> entry : apiDataMaps.entrySet()) {
			String apiName = entry.getKey();
			List<ChartDTO> apiData = entry.getValue();

			// Create a new data map for the API Name
			LinkedHashMap<String, Object> apiDataMap = new LinkedHashMap<>();
			apiDataMap.put("apiName", apiName);

			// Create a list to store the data for each date
			List<LinkedHashMap<String, Object>> apiDateData = new ArrayList<>();

			if (filter.equals("week")) {
				LocalDate currentDate = LocalDate.now();
				LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

				for (int i = 0; i < 7; i++) {
					LocalDate date = startOfWeek.plusDays(i);

					// Find the ChartDTO object for the current date
					ChartDTO chartDTO = apiData.stream()
							.filter(data -> data.getX().getDayOfMonth() == date.getDayOfMonth()).findFirst()
							.orElse(null);

					// Create a data map for the date and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", date);
					data.put("totalFault", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("month")) {
				LocalDate currentDate = LocalDate.now();
				int daysInMonth = currentDate.lengthOfMonth();

				for (int i = 1; i <= daysInMonth; i++) {
					LocalDate dateI = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), i);

					// Find the ChartDTO object for the current date
					ChartDTO chartDTO = apiData.stream().filter(data -> data.getX().equals(dateI)).findFirst()
							.orElse(null);

					// Create a data map for the date and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", dateI);
					data.put("totalFault", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("year")) {
				LocalDate currentDate = LocalDate.now();

				for (int i = 1; i <= 12; i++) {
					YearMonth yearMonth = YearMonth.of(currentDate.getYear(), i);

					// Find the ChartDTO object for the current year-month
					ChartDTO chartDTO = apiData.stream().filter(data -> YearMonth.from(data.getX()).equals(yearMonth))
							.findFirst().orElse(null);

					// Create a data map for the year-month and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					data.put("x", yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
					data.put("totalFault", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			} else if (filter.equals("today")) {
				LocalDate currentDate = LocalDate.now();

				for (int i = 0; i < 24; i++) {
					LocalDateTime dateTime = LocalDateTime.of(currentDate, LocalTime.of(i, 0));

					// Find the ChartDTO object for the current hour
					ChartDTO chartDTO = apiData.stream()
							.filter(data -> data.getXHour() != null && data.getXHour().equals(dateTime)).findFirst()
							.orElse(null);

					// Create a data map for the hour and total usage
					LinkedHashMap<String, Object> data = new LinkedHashMap<>();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
					data.put("x", dateTime.format(formatter));
					data.put("totalFault", chartDTO != null ? chartDTO.getTotalUsage() : 0);

					// Add the data map to the list
					apiDateData.add(data);
				}
			}

			// Add the date data list to the API Name data map
			apiDataMap.put("data", apiDateData);

			// Add the API Name data map to the finalResult
			finalResult.add(apiDataMap);
		}

		return finalResult;
	}
	
	

	public List<DashboardPercentageDTO> getApiUsageByApi(Integer top,String keyType) {
		    StringBuilder query = new StringBuilder();
		    query.append("SELECT\n")
		         .append("    DATA_USAGE_API.API_ID,\n")
		         .append("    API_NAME,\n")
		         .append("    COUNT(*) AS row_count,\n")
		         .append("    (COUNT(*) / (\n")
		         .append("        SELECT COUNT(*)\n")
		         .append("        FROM DATA_USAGE_API\n")
		         .append("        LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n")
		         .append("        WHERE 1 = 1\n")
		         .append("    AND (API_CREATOR_TENANT_DOMAIN = :tenantDomain)\n")
		         .append("    AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username)\n")
		         .append("    AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN')\n")
		         .append("    AND (:keyType IS NULL OR DATA_USAGE_API.KEY_TYPE = :keyType)\n")
		         .append("    ) * 100) AS percentage\n")
		         .append("FROM\n")
		         .append("    DATA_USAGE_API\n")
		         .append("    LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n")
		         .append("WHERE 1 = 1\n")
		         .append("    AND (API_CREATOR_TENANT_DOMAIN = :tenantDomain)\n")
		         .append("    AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username)\n")
		         .append("    AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN')\n")
		         .append("    AND (:keyType IS NULL OR DATA_USAGE_API.KEY_TYPE = :keyType)\n")
		         .append("GROUP BY\n")
		         .append("    API_ID,\n")
		         .append("    API_NAME\n")
		         .append("ORDER BY\n")
		         .append("    row_count DESC\n")
		         .append("LIMIT :top");
		    


		Map<String, Object> params = new HashMap<>();
		params.put("isAdmin", Utils.isAdmin());
		params.put("username", Utils.getUsernameWithTenantDomain());
		params.put("tenantDomain", Utils.getTenantFromUsername());
		params.put("top", top);
		params.put("keyType", keyType);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		return namedParameterJdbcTemplate.query(query.toString(), parameters, new DashboardApiPercentageMapper());
	}
	public List<DashboardPercentageDTO> getApiUsageByApplication(Integer top,String keyType) {

		StringBuilder query = new StringBuilder();
	    query.append("SELECT\n")
	         .append("    APPLICATION_ID,\n")
	         .append("    APPLICATION_NAME,\n")
	         .append("    COUNT(*) AS row_count,\n")
	         .append("    (COUNT(*) / (\n")
	         .append("            SELECT COUNT(*)\n")
	         .append("            FROM DATA_USAGE_API\n")
	         .append("            LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n")
	         .append("            WHERE 1=1\n")
	         .append("    AND (API_CREATOR_TENANT_DOMAIN = :tenantDomain)\n")
	         .append("    AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username)\n")
	         .append("    AND (:keyType IS NULL OR DATA_USAGE_API.KEY_TYPE = :keyType)\n")
	         .append("    AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN')\n")
	         .append("        ) * 100\n")
	         .append("    ) AS percentage\n")
	         .append("FROM\n")
	         .append("    DATA_USAGE_API\n")
	         .append("    LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n")
	         .append("WHERE 1=1\n")
	         .append("    AND (API_CREATOR_TENANT_DOMAIN = :tenantDomain)\n")
	         .append("    AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username)\n")
	         .append("    AND (:keyType IS NULL OR DATA_USAGE_API.KEY_TYPE = :keyType)\n")
	         .append("    AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN')\n")
	         .append("GROUP BY\n")
	         .append("    APPLICATION_ID,\n")
	         .append("    APPLICATION_NAME\n")
	         .append("ORDER BY\n")
	         .append("    row_count DESC\n")
	         .append("LIMIT :top");

		Map<String, Object> params = new HashMap<>();
		params.put("isAdmin", Utils.isAdmin());
		params.put("username", Utils.getUsernameWithTenantDomain());
		params.put("tenantDomain", Utils.getTenantFromUsername());
		params.put("top", top);
		params.put("keyType", keyType);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		return namedParameterJdbcTemplate.query(query.toString(), parameters, new DashboardAppPercentageMapper());
	}

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(Integer top,String keyType) {
	    StringBuilder query = new StringBuilder();
	    query.append("SELECT\n")
	         .append("    response_category,\n")
	         .append("    total_count AS row_count,\n")
	         .append("    ROUND((total_count / SUM(total_count) OVER ()) * 100, 2) AS percentage\n")
	         .append("FROM\n")
	         .append("    (\n")
	         .append("        SELECT\n")
	         .append("            CASE\n")
	         .append("                WHEN PROXY_RESPONSE_CODE BETWEEN 100 AND 199 THEN '1xx'\n")
	         .append("                WHEN PROXY_RESPONSE_CODE BETWEEN 200 AND 299 THEN '2xx'\n")
	         .append("                WHEN PROXY_RESPONSE_CODE BETWEEN 300 AND 399 THEN '3xx'\n")
	         .append("                WHEN PROXY_RESPONSE_CODE BETWEEN 400 AND 499 THEN '4xx'\n")
	         .append("                WHEN PROXY_RESPONSE_CODE BETWEEN 500 AND 599 THEN '5xx'\n")
	         .append("                ELSE 'OTHERS'\n")
	         .append("            END AS response_category,\n")
	         .append("            COUNT(*) AS total_count\n")
	         .append("        FROM\n")
	         .append("            DATA_USAGE_API\n")
	         .append("            LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n")
	         .append("        WHERE\n")
	         .append("    		  (DATA_USAGE_API.API_CREATOR_TENANT_DOMAIN = :tenantDomain)\n")
	         .append("    		  AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username)\n")
	         .append("            AND DATA_USAGE_API.APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN')\n")
	         .append("    		  AND (:keyType IS NULL OR DATA_USAGE_API.KEY_TYPE = :keyType)\n")
	         .append("        GROUP BY\n")
	         .append("            response_category\n")
	         .append("        ORDER BY\n")
	         .append("            total_count DESC\n")
	         .append("        LIMIT :top ")
	         .append("    ) AS subquery;");



		Map<String, Object> params = new HashMap<>();
		params.put("isAdmin", Utils.isAdmin());
		params.put("username", Utils.getUsernameWithTenantDomain());
		params.put("tenantDomain", Utils.getTenantFromUsername());
		params.put("top", top);
		params.put("keyType", keyType);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query.toString());
		return namedParameterJdbcTemplate.query(query.toString(), parameters, new DashboardResCodePercentageMapper());
	}

	public TotalReportDashboard getDashboardTotalReport() {
		TotalReportDashboard totalReportDashboard = new TotalReportDashboard();
		if (Utils.isAdmin()==false) {
			totalReportDashboard.setTotalUnpaid(getTotalUnpaidInvoicesByUsername());
			totalReportDashboard.setTotalResponseFault(getTotalResponseFaultByUsername());
			totalReportDashboard.setTotalApplication(getTotalAppsByUsername());
			totalReportDashboard.setTotalSubscriptionAPI(getTotalSubscriptionAPIByUsername());
		} else {
			totalReportDashboard.setTotalUnpaid(getTotalUnpaidInvoicesByUsername());
			totalReportDashboard.setTotalApi(getTotalAPIsByUsername());
			totalReportDashboard.setTotalApplication(getTotalAppsByUsername());
			totalReportDashboard.setTotalSubscriber(getTotalSubscriberByUsername());
		}

		return totalReportDashboard;
	}

	public int getTotalAPIsByUsername() {
		String query = "SELECT COUNT(*) AS totalAPI " + "FROM AM_API WHERE "
				+ " AM_API.ORGANIZATION = :tenantDomain "
				+ " AND (:isAdmin = true OR AM_API.CREATED_BY = :username)";

		try {
			// Create parameters for the named query
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("tenantDomain", Utils.getTenantFromUsername());
			params.addValue("isAdmin", Utils.isAdmin());
			params.addValue("username", Utils.getUsernameWithTenantDomain());

			// Execute the query and retrieve the result
			return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getTotalAppsByUsername() {
		String query = "SELECT COUNT(*) AS totalApps " + "FROM AM_APPLICATION " 
				+ "LEFT JOIN " 
				+ "AM_SUBSCRIBER ON "
				+ "AM_SUBSCRIBER.SUBSCRIBER_ID =AM_APPLICATION.SUBSCRIBER_ID " 
				+ "WHERE AM_APPLICATION.ORGANIZATION =:tenantDomain "
				+ "AND (:isAdmin = true OR AM_APPLICATION.CREATED_BY = :username)";

		try {
			// Create parameters for the named query
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("tenantDomain", Utils.getTenantFromUsername());
			params.addValue("isAdmin", Utils.isAdmin());
			params.addValue("username", Utils.getUsernameWithTenantDomain());
			// Execute the query and retrieve the result
			return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getTotalSubscriberByUsername() {
		String query = "SELECT COUNT(*) AS totalSubscriber " 
				+ "FROM AM_SUBSCRIBER " 
				+ "WHERE " 
				+ " AM_SUBSCRIBER.TENANT_ID =(SELECT COALESCE((SELECT x.UM_ID  "
				+ "            FROM shared_database.UM_TENANT x  "
				+ "            WHERE x.UM_DOMAIN_NAME = :tenantDomain),-1234) as UM_ID) "
				+ "AND (:isAdmin = true OR AM_SUBSCRIBER.USER_ID = :username)";

		try {
			// Create parameters for the named query
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("tenantDomain", Utils.getTenantFromUsername());
			params.addValue("isAdmin", Utils.isAdmin());
			params.addValue("username", Utils.getUsernameWithTenantDomain());
			// Execute the query and retrieve the result
			return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getTotalSubscriptionAPIByUsername() {
		String query = "SELECT COUNT(*) AS totalSubscriptionAPI " 
				+ "FROM AM_SUBSCRIPTION "
				+ "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID "
				+ "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID "
				+ "LEFT JOIN AM_API ON AM_API.API_ID = AM_SUBSCRIPTION.API_ID "
				+ "WHERE  AM_APPLICATION.ORGANIZATION =:tenantDomain "
				+ "AND (:isAdmin = true OR AM_APPLICATION.CREATED_BY = :username)";

		try {
			// Create parameters for the named query
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("tenantDomain", Utils.getTenantFromUsername());
			params.addValue("isAdmin", Utils.isAdmin());
			params.addValue("username", Utils.getUsernameWithTenantDomain());
			
			// Execute the query and retrieve the result
			return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getTotalResponseFaultByUsername() {
		String query = "SELECT COUNT(*) AS totalResponseFault " 
				+ "FROM DATA_USAGE_API "
				+ "LEFT JOIN "+dbUtilsBilling.getSchemaName()+".subscription s on s.subscription_id = DATA_USAGE_API.subscription_id "
				+ "WHERE DATA_USAGE_API.PROXY_RESPONSE_CODE BETWEEN 200 AND 299  "
				+ " AND (du.API_CREATOR_TENANT_DOMAIN = :tenantDomain) "
				+ " AND (:isAdmin = true  OR DATA_USAGE_API.APPLICATION_OWNER = :username) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') ";

		try {
			// Create parameters for the named query
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("tenantDomain", Utils.getTenantFromUsername());
			params.addValue("isAdmin", Utils.isAdmin());
			params.addValue("username", Utils.getUsernameWithTenantDomain());

			// Execute the query and retrieve the result
			return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getTotalUnpaidInvoicesByUsername() {
		String query = "SELECT COUNT(i.id) as total FROM invoice i "
				+ "WHERE i.status = 1 AND i.tenant_domain = ? and (? = true OR i.customer_id  = ?)";

		try (Connection connection = dbUtilsBilling.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {

			preparedStatement.setString(1, Utils.getTenantFromUsername());
			preparedStatement.setBoolean(2, Utils.isAdmin());
			preparedStatement.setString(3, Utils.getUsernameWithTenantDomain());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				
				if (resultSet.next()) {
					 int res=resultSet.getInt("total");
					 connection.close();
					 preparedStatement.close();
					 resultSet.close();
					return res;
				} else {
					 connection.close();
					 preparedStatement.close();
					 resultSet.close();
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public LinkedHashMap<String, Object> getUsagePercentage(Integer top, Boolean byApplication,
			Boolean byResponseCode, Boolean byApi, String keyType) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		if (byApplication)
			result.put("byApplication", getApiUsageByApplication( top, keyType));
		if (byApi)
			result.put("byApi", getApiUsageByApi( top, keyType));
		if (byResponseCode)
			result.put("byResponseCode", getApiUsageByResponseCode( top, keyType));
		return result;
	}
}
