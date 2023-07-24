package com.wso2.swamedia.reportusageapi.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.swamedia.reportusageapi.DBUtilsUser;
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
import com.wso2.swamedia.reportusageapi.mapper.DashboardApiPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardAppPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardResCodePercentageMapper;
import com.wso2.swamedia.reportusageapi.repo.AmApiRepository;
import com.wso2.swamedia.reportusageapi.repo.DataUsageApiRepository;

@Service
public class ReportUsageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageService.class);


	@Value("${spring.billing-datasource.url}")
	private String databaseUrl;
	
	public String getDatabaseNameFromService() {
		// Extract database name from URL
		String[] urlParts = databaseUrl.split("/");
		String databaseName = urlParts[urlParts.length - 1];
		return databaseName;
	}
	
	@Autowired
	private DataUsageApiRepository dataUsageApiRepository;

	@Autowired
	private AmApiRepository amApiRepository;

	@Autowired
	private DBUtilsUser dbUtilsUser;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public MonthlySummary getMonthlyReport(Integer year, Integer month, String applicationId, String apiId,
			String username, int page, int size, String search, String organization,Boolean showDeleted) throws Exception {
		LOGGER.info("Retrieving monthly report for year: {}, month: {}, username: {}", year, month, username);

		MonthlySummary monthlySummary = new MonthlySummary();
		try {
			Map<String, Object> dataTotal = dataUsageApiRepository.getTotalApisAndRequestsByOwnerAndFilters(username,
					year, month, apiId, applicationId, organization,showDeleted);
			monthlySummary.setTotalApis(Integer.valueOf(dataTotal.get("total_apis").toString()));
			monthlySummary.setRequestCount(Integer.valueOf(dataTotal.get("total_request").toString()));
			monthlySummary.setTotalCustomers(Integer.valueOf(dataTotal.get("total_customer").toString()));
		} catch (Exception e) {
			String error = String.format("Error retrieving total APIs and requests: {}", e.getMessage());
			LOGGER.error(error);
			// Handle the exception or throw a custom exception
			throw new Exception(e.getMessage());
		}
//		List<OrganizationDTO> organizationDTOs = getOrganizations();
		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Object[]> result = dataUsageApiRepository.getMonthlyTotalRowByGroupByWithSearchAndPageable(username,
					year, month, apiId, showDeleted,applicationId, search, organization, pageable);

			Page<MonthlySummary.ApiDetails> pageM = result.map(row -> {
//				OrganizationDTO org = findOrganizationByUsername(organizationDTOs, (String) row[2]);
				return new MonthlySummary.ApiDetails((String) row[0], (String) row[3], (String) row[1], (String) row[4],
						(String) row[2], (BigInteger) row[5], (String) row[6], (String) row[7]);
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
			String searchFilter, Pageable pageable, Integer year, Integer month,Boolean showDeletedSubscription) throws Exception {
		LOGGER.info("Retrieving API Monthly detail log report for owner: {}, applicationId: {}, apiId: {}", owner,
				applicationId, apiId);

		Page<MonthlySummaryDetails> pageM = null;
		try {
			Page<Object[]> result = dataUsageApiRepository.getMonthlyDetailLog(pageable, owner, applicationId, apiId,
					searchFilter, year, month,showDeletedSubscription);

			pageM = result.map(row -> {
				String requestTimestamp = (String) row[0];
				String resource = (String) row[1];
				Integer proxyResponseCode = (Integer) row[2];
				String apiIdRes = (String) row[3];
				String applicationIdres = (String) row[4];
				String apiNameQ = (String) row[5];
				String appNameQ = (String) row[6];
				String organtization = (String) row[7];

				return new MonthlySummaryDetails(requestTimestamp, resource, proxyResponseCode, apiIdRes,
						applicationIdres, apiNameQ, appNameQ, organtization);
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
			String username, int page, int size, String search,Boolean showDeletedSubscription) throws Exception {
		LOGGER.info("Retrieving resource summary for year: {}, month: {}, resource: {}, username: {}", year, month,
				resource, username);

		ResourceSummary resourceSummary = new ResourceSummary();
		try {
			Map<String, Object> dataTotal = dataUsageApiRepository.getResourceSumTotal(username, year, month, apiId,
					resource,showDeletedSubscription);
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
					search, pageable,showDeletedSubscription);

			Page<ResourceSummary.ApiDetails> pageM = result.map(row -> {
				String apiNameQ = (String) row[0];
				String apiVersionQ = (String) row[1];
				String resourceQ = (String) row[2];
				String apiMethodQ = (String) row[3];
				BigInteger count = (BigInteger) row[4];
				String apiIdQ = (String) row[5];
				return new ResourceSummary.ApiDetails(apiNameQ, apiVersionQ, resourceQ, apiMethodQ, count, apiIdQ,
						null, null);
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
			String searchFilter, Pageable pageable,Boolean showDeletedSubscription) throws Exception {
		LOGGER.info("Retrieving resource detail log for owner: {}, resource: {}, apiId: {}", owner, resource, apiId);

		Page<ResourceSummaryDetails> pageM = null;
		List<OrganizationDTO> organizationDTOs = getOrganizations();
		try {
			Page<Object[]> result = dataUsageApiRepository.getDetailLogResourceSum(pageable, owner, resource, apiId,
					searchFilter,showDeletedSubscription);

			pageM = result.map(row -> {
				String appName = (String) row[0];
				String apiName = (String) row[1];
				BigInteger requestCount = (BigInteger) row[2];
				BigInteger countNOK = (BigInteger) row[3];
				BigInteger countOK = (BigInteger) row[4];
				String apiIds = (String) row[5];
				String appId = (String) row[6];
				String appOwner = (String) row[7];
				OrganizationDTO org = findOrganizationByUsername(organizationDTOs, appOwner);
				return new ResourceSummaryDetails(appId, appName, apiIds, apiName, requestCount, countOK, countNOK,
						appOwner, org != null ? org.getValue() : null);

			});
		} catch (Exception e) {
			String error = String.format("Error retrieving resource detail log: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		LOGGER.info("Resource detail log retrieval completed");

		return pageM;
	}

	public List<DashboardPercentageDTO> getApiUsageByApi(LocalDate startDate, LocalDate endDate, String username) {
		String query = "SELECT API_ID, API_NAME, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY API_ID, API_NAME";

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardApiPercentageMapper());
	}
	
	
	public List<LinkedHashMap<String, Object>> getPlanByPaymentType(Integer subsTypeId,Boolean isDeployed) {
	    String sqlQuery = "SELECT * FROM AM_POLICY_SUBSCRIPTION ";
	    int val = (isDeployed) ? 1 : 0;
	    if (subsTypeId == 2) {
	        sqlQuery += "WHERE CUSTOM_ATTRIBUTES = '[{\"name\":\"type_subscription\",\"value\":\"time\"}]' AND IS_DEPLOYED = "+val;
	    }else {
	    	sqlQuery+="WHERE IS_DEPLOYED = "+val;
	    }

	    return namedParameterJdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
	        LinkedHashMap<String, Object> policyInfo = new LinkedHashMap<>();

	        policyInfo.put("policyId", rs.getInt("POLICY_ID"));
	        policyInfo.put("name", rs.getString("NAME"));
	        policyInfo.put("displayName", rs.getString("DISPLAY_NAME"));
	        policyInfo.put("tenantId", rs.getInt("TENANT_ID"));
	        policyInfo.put("description", rs.getString("DESCRIPTION"));
	        policyInfo.put("quotaType", rs.getString("QUOTA_TYPE"));
	        policyInfo.put("quota", rs.getInt("QUOTA"));
	        policyInfo.put("quotaUnit", rs.getString("QUOTA_UNIT"));
	        policyInfo.put("unitTime", rs.getInt("UNIT_TIME"));
	        policyInfo.put("timeUnit", rs.getString("TIME_UNIT"));
	        policyInfo.put("rateLimitCount", rs.getInt("RATE_LIMIT_COUNT"));
	        policyInfo.put("rateLimitTimeUnit", rs.getString("RATE_LIMIT_TIME_UNIT"));
	        policyInfo.put("isDeployed", rs.getBoolean("IS_DEPLOYED"));
	        policyInfo.put("customAttributes", rs.getString("CUSTOM_ATTRIBUTES"));
	        policyInfo.put("stopOnQuotaReach", rs.getBoolean("STOP_ON_QUOTA_REACH"));
	        policyInfo.put("billingPlan", rs.getString("BILLING_PLAN"));
	        policyInfo.put("uuid", rs.getString("UUID"));
	        policyInfo.put("monetizationPlan", rs.getString("MONETIZATION_PLAN"));
	        policyInfo.put("fixedRate", rs.getString("FIXED_RATE"));
	        policyInfo.put("billingCycle", rs.getString("BILLING_CYCLE"));
	        policyInfo.put("pricePerRequest", rs.getString("PRICE_PER_REQUEST"));
	        policyInfo.put("currency", rs.getString("CURRENCY"));
	        policyInfo.put("maxComplexity", rs.getInt("MAX_COMPLEXITY"));
	        policyInfo.put("maxDepth", rs.getInt("MAX_DEPTH"));
	        policyInfo.put("connectionsCount", rs.getInt("CONNECTIONS_COUNT"));

	        return policyInfo;
	    });
	}


	public List<DashboardPercentageDTO> getApiUsageByApplication(LocalDate startDate, LocalDate endDate,
			String username) {
		String query = "SELECT APPLICATION_ID, APPLICATION_NAME, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY APPLICATION_ID, APPLICATION_NAME";

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardAppPercentageMapper());
	}

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(LocalDate startDate, LocalDate endDate,
			String username) {
		String query = "SELECT PROXY_RESPONSE_CODE, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY PROXY_RESPONSE_CODE";

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardResCodePercentageMapper());
	}

	private String getOptionalDateRangeCondition(LocalDate startDate, LocalDate endDate) {
		StringBuilder condition = new StringBuilder();
		if (startDate != null && endDate != null) {
			condition.append(" AND REQUEST_TIMESTAMP >= :startDate AND REQUEST_TIMESTAMP <= :endDate");
		} else if (startDate != null) {
			condition.append(" AND REQUEST_TIMESTAMP >= :startDate");
		} else if (endDate != null) {
			condition.append(" AND REQUEST_TIMESTAMP <= :endDate");
		}
		return condition.toString();
	}

	private Map<String, Object> getOptionalDateRangeNamedParams(LocalDate startDate, LocalDate endDate) {
		Map<String, Object> namedParams = new HashMap<>();
		if (startDate != null) {
			namedParams.put("startDate", startDate);
		}
		if (endDate != null) {
			namedParams.put("endDate", endDate);
		}
		return namedParams;
	}

	public LinkedHashMap<String, Object> getUsagePercentage(LocalDate startDate, LocalDate endDate, String username) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		result.put("byApplication", getApiUsageByApplication(startDate, endDate, username));
		result.put("byApi", getApiUsageByApi(startDate, endDate, username));
		result.put("byResponseCode", getApiUsageByResponseCode(startDate, endDate, username));
		return result;
	}

	public List<Map<String, Object>> getApiNameAndId(String owner, String organization) {
		String query = "SELECT DISTINCT AM_API.API_ID, AM_API.API_NAME,AM_API.API_UUID,AM_SUBSCRIBER.USER_ID  "
				+ "FROM AM_SUBSCRIPTION " 
				+ "LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID = AM_API.API_ID "
				+ "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID "
				+ "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID "
				+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
				+ "AND attr.UM_ATTR_NAME = 'organizationName' "
				+ "WHERE (:owner IS NULL OR AM_SUBSCRIBER.USER_ID  = :owner) "
				+ "AND (:organizationName IS NULL OR attr.UM_ATTR_VALUE = :organizationName)";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		parameters.addValue("organizationName", organization);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			apiInfo.put("apiId", rs.getString("API_ID"));
			apiInfo.put("apiUUID", rs.getString("API_UUID"));
			apiInfo.put("apiName", rs.getString("API_NAME"));
			return apiInfo;
		});
	}
	
	public List<Map<String, Object>> getApis(String owner, String organization) {
		String query = "SELECT DISTINCT AM_API.API_ID, AM_API.API_NAME,AM_API.API_UUID "
				+ "FROM AM_SUBSCRIPTION " 
				+ "LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID = AM_API.API_ID "
				+ "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID "
				+ "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID "
				+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
				+ "AND attr.UM_ATTR_NAME = 'organizationName' "
				+ "WHERE (:owner IS NULL OR AM_SUBSCRIBER.USER_ID  = :owner) "
				+ "AND (:organizationName IS NULL OR attr.UM_ATTR_VALUE = :organizationName)";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		parameters.addValue("organizationName", organization);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			apiInfo.put("apiId", rs.getString("API_ID"));
			apiInfo.put("apiUUID", rs.getString("API_UUID"));
			apiInfo.put("apiName", rs.getString("API_NAME"));
			return apiInfo;
		});
	}

	public List<Map<String, Object>> getYears(String owner) {
		String query = "SELECT DISTINCT YEAR(REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') ";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			Year year = Year.of(rs.getInt("year"));
			apiInfo.put("year", year);
			return apiInfo;
		});
	}

	public List<Map<String, Object>> getCustomers(String owner) {
		String sqlQuery = "SELECT uu.*, attr.UM_ATTR_VALUE AS organizationName " + "FROM apim_db_test.AM_SUBSCRIBER as2 "
				+ "JOIN apim_shareddb_test.UM_USER uu ON as2.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
				+ "AND attr.UM_ATTR_NAME = 'organizationName'";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
//		parameters.addValue("owner", owner);

		return namedParameterJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) -> {
			Map<String, Object> customers = new HashMap<>();
			customers.put("username", rs.getString("UM_USER_NAME"));
			customers.put("userId", rs.getString("UM_USER_ID"));
			customers.put("tenantId", rs.getString("UM_TENANT_ID"));
			customers.put("organizationName", rs.getString("organizationName"));
			return customers;
		});
	}

	public int getTotalCustomers(String username) {
		String sqlQuery = "select COUNT(DISTINCT UM_ATTR_VALUE)" + "from apim_shareddb_test.UM_USER_ATTRIBUTE "
				+ "where UM_ATTR_NAME='organizationName'";
		try {

			Integer result = namedParameterJdbcTemplate.queryForObject(sqlQuery, new MapSqlParameterSource(),
					Integer.class);
			return (result != null ? result : 0);

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public List<Map<String, Object>> getCustomersv2(String owner) {
		String sqlQuery = "select DISTINCT UM_ATTR_VALUE as organizationName " + "from apim_shareddb_test.UM_USER_ATTRIBUTE "
				+ "where UM_ATTR_NAME='organizationName'";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
//		parameters.addValue("owner", owner);

		return namedParameterJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) -> {
			Map<String, Object> customers = new HashMap<>();
//			customers.put("username", rs.getString("UM_USER_NAME"));
//			customers.put("userId", rs.getString("UM_USER_ID"));
//			customers.put("tenantId", rs.getString("UM_TENANT_ID"));
			customers.put("organizationName", rs.getString("organizationName"));
			return customers;
		});
	}

	public List<Map<String, Object>> getMonth(String owner, int year) {
		String query = "SELECT DISTINCT MONTH(REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API "
				+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner) AND "
				+ " (:year IS NULL OR  YEAR(REQUEST_TIMESTAMP) = :year ) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') ";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		parameters.addValue("year", year);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			LinkedHashMap<String, Object> apiInfo = new LinkedHashMap<>();
			Month month = Month.of(rs.getInt("year"));
			apiInfo.put("monthName", month);
			apiInfo.put("monthNumber", month.getValue());
			return apiInfo;
		});
	}

	public List<Map<String, Object>> getApiResourceByAPI(String owner, String apiId) {
		String query = "SELECT DISTINCT API_RESOURCE_TEMPLATE  FROM DATA_USAGE_API WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner)"
				+ " AND (:apiId IS NULL OR API_ID = :apiId) "
				+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') AND API_RESOURCE_TEMPLATE IS NOT NULL";
		LOGGER.info(query);
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		parameters.addValue("apiId", apiId);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			apiInfo.put("resource", rs.getString("API_RESOURCE_TEMPLATE"));
			return apiInfo;
		});
	}

	public List<Map<String, Object>> getVersions(String apiName) {
		String query = "SELECT API_VERSION,API_NAME ,API_UUID  FROM AM_API WHERE "
				+ " (:apiName IS NULL OR API_NAME = :apiName)";
		LOGGER.info(query);
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("apiName", apiName);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			apiInfo.put("version", rs.getString("API_VERSION"));
			apiInfo.put("apiId", rs.getString("API_UUID"));
			apiInfo.put("apiName", rs.getString("API_NAME"));
			return apiInfo;
		});
	}

	public List<OrganizationDTO> getOrganizations() throws Exception {
		List<OrganizationDTO> result = new ArrayList<>();
		String sql = "SELECT U.UM_USER_NAME as username ,UA.UM_ATTR_NAME, UA.UM_ATTR_VALUE as value\n"
				+ "FROM UM_USER U\n" + "JOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID\n"
				+ "WHERE  UM_ATTR_NAME ='organizationName'";

		try (Connection connection = dbUtilsUser.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				OrganizationDTO organization = new OrganizationDTO();
				organization.setUsername(resultSet.getString("username"));
				organization.setValue(resultSet.getString("value"));
				result.add(organization);
			}
			connection.close();
			resultSet.close();
			statement.close();
			} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Error occurred while retrieving organization details.", e);
		}
		return result;
	}

	public Page<TableRemainingDayQuota> getSubscriptionsRemaining(String owner, Pageable pageable) {
		String query = "SELECT " 
				+ "attr.UM_ATTR_VALUE as organizationName, uu.UM_USER_NAME, "
				+ "subs.subs_state_id , "
				+ "AM_SUBSCRIPTION.SUBSCRIPTION_ID, " + "AM_APPLICATION.NAME AS APPLICATION_NAME, "
				+ "AM_API.API_NAME  AS API_NAME, " + "AM_POLICY_SUBSCRIPTION.NAME AS POLICY_NAME, "
				+ "subs.quota AS INIT_QUOTA, " + "AM_POLICY_SUBSCRIPTION.CUSTOM_ATTRIBUTES,"
				+ "COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS API_USAGE, "
				+ "COALESCE(subs.quota, 0) - COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS REMAINING_QUOTA, "
				+ "AM_SUBSCRIPTION.CREATED_TIME AS START_DATE, "
				+ "DATEDIFF(DATE_ADD(AM_SUBSCRIPTION.CREATED_TIME, INTERVAL 30 DAY), CURDATE()) AS REMAINING_DAYS,"
				+ " AM_POLICY_SUBSCRIPTION.TIME_UNIT AS TIME_UNIT, " + "AM_POLICY_SUBSCRIPTION.UNIT_TIME AS UNIT_TIME,subs.notes "
				+ "FROM "+ getDatabaseNameFromService() + ".subscription subs "
				+ "LEFT JOIN AM_SUBSCRIPTION  ON  subs.subscription_id = AM_SUBSCRIPTION.UUID COLLATE utf8mb4_unicode_ci "
				+ "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID "
				+ "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID  = AM_SUBSCRIBER.SUBSCRIBER_ID "
				+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' "
				+ "LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID  = AM_API.API_ID "
				+ "LEFT JOIN AM_POLICY_SUBSCRIPTION ON AM_SUBSCRIPTION.TIER_ID = AM_POLICY_SUBSCRIPTION.NAME "
				+ "LEFT JOIN ( "
				+ "SELECT \n"
				+ "		DATA_USAGE_API.SUBSCRIPTION_UUID ,\n"
				+ "		DATA_USAGE_API.API_ID,\n"
				+ "		DATA_USAGE_API.APPLICATION_ID,\n"
				+ "		COUNT(*) AS USAGE_COUNT ,\n"
				+ "		APPLICATION_OWNER\n"
				+ "	FROM\n"
				+ "		DATA_USAGE_API\n"
				+ "	LEFT JOIN billing_db.subscription subs on\n"
				+ "		subs.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n"
				+ "	WHERE\n"
				+ "		DATA_USAGE_API.REQUEST_TIMESTAMP BETWEEN subs.start_date AND subs.end_date AND DATA_USAGE_API.KEY_TYPE = 'PRODUCTION' \n"
				+ "	GROUP BY\n"
				+ "		SUBSCRIPTION_UUID,\n"
				+ "		API_ID,\n"
				+ "		APPLICATION_ID ,\n"
				+ "		APPLICATION_OWNER  "
				+ ") AS DATA_USAGE ON subs.subscription_id  = DATA_USAGE.SUBSCRIPTION_UUID "
				+ "WHERE " 
//				+ "AM_SUBSCRIPTION.SUBS_CREATE_STATE = 'SUBSCRIBE' "
//				+ "AND AM_SUBSCRIPTION.SUB_STATUS = 'UNBLOCKED' "
//				+ "AND 
				+ "AM_POLICY_SUBSCRIPTION.BILLING_PLAN != 'FREE' "
				+ "AND (:owner IS NULL OR AM_SUBSCRIBER.USER_ID = :owner) "
				+ "ORDER BY REMAINING_DAYS ASC, REMAINING_QUOTA DESC, API_USAGE DESC;";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		List<TableRemainingDayQuota> dtos = namedParameterJdbcTemplate.query(query, parameters, (resultSet, rowNum) -> {
			TableRemainingDayQuota dto = new TableRemainingDayQuota();
			String subsStateId=resultSet.getString("subs_state_id");
			dto.setSubsStateId(subsStateId);
//			
			// Set values to the dto from the resultSet
			dto.setSubscriptionId(resultSet.getString("SUBSCRIPTION_ID"));
			dto.setApplicationName(resultSet.getString("APPLICATION_NAME"));
			dto.setApiName(resultSet.getString("API_NAME"));
			dto.setPolicyName(resultSet.getString("POLICY_NAME"));
			dto.setOrganization(resultSet.getString("organizationName"));			
			dto.setApplicationOwner(resultSet.getString("UM_USER_NAME"));
			// Retrieve the blob as a string
			String jsonString = resultSet.getString("CUSTOM_ATTRIBUTES");

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = jsonString != null && !jsonString.isEmpty() && !jsonString.isBlank()
						? objectMapper.readTree(jsonString)
						: null;
				if (jsonNode != null && !jsonNode.isEmpty()) {
					List<JsonNode> nodes = jsonNode != null ? Arrays.asList(jsonNode.elements().next())
							: new ArrayList<>();
					Optional<String> typeSubscriptionValue = nodes.stream()
							.filter(node -> "type_subscription".equals(node.get("name").asText()))
							.map(node -> node.get("value").asText()).findFirst();
					String value = typeSubscriptionValue.orElse("");

					if (value.equalsIgnoreCase("time")) {

						dto.setStartDate(resultSet.getDate("START_DATE"));

						if (resultSet.getString("TIME_UNIT").equals("days")) {
							dto.setQuota(resultSet.getInt("UNIT_TIME"));
						} else if (resultSet.getString("TIME_UNIT").equals("months")) {
							dto.setQuota(resultSet.getInt("UNIT_TIME") * 30);
						} else if (resultSet.getString("TIME_UNIT").equals("years")) {
							dto.setQuota(resultSet.getInt("UNIT_TIME") * 365);
						} else {
							dto.setQuota(0);
						}

						Calendar calendar = Calendar.getInstance();
						calendar.setTime(dto.getStartDate());

						calendar.add(Calendar.DAY_OF_YEAR, dto.getQuota() + 1); // Add 1 day to the current date

						Date newDate = calendar.getTime();
						long diffInMilliseconds = newDate.getTime() - new Date().getTime();

						// Convert milliseconds to days
						Integer remainingDays = (int) TimeUnit.DAYS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
						remainingDays = remainingDays < 0 ? 0 : remainingDays;
						dto.setRemaining(dto.getQuota().equals(0) ? 0 : remainingDays);
						dto.setTypeSubscription("time");
					} else {
						dto.setQuota(resultSet.getInt("INIT_QUOTA"));
						dto.setApiUsage(resultSet.getInt("API_USAGE"));
						dto.setRemaining(resultSet.getInt("REMAINING_QUOTA")<0?0:resultSet.getInt("REMAINING_QUOTA"));
						dto.setTypeSubscription("quota");
					}
					dto.setNotes(resultSet.getString("notes"));				
					}

			} catch (JsonMappingException e) {
				// Log the exception
				LOGGER.error("JsonMappingException occurred: ", e);
			} catch (JsonProcessingException e) {
				// Log the exception
				LOGGER.error("JsonProcessingException occurred: ", e);
			}
			return dto;
		});

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<TableRemainingDayQuota> pagedDtos;

		if (dtos.size() < startItem) {
			pagedDtos = List.of();
		} else {
			int toIndex = Math.min(startItem + pageSize, dtos.size());
			pagedDtos = dtos.subList(startItem, toIndex);
		}

		return new PageImpl<>(pagedDtos, pageable, dtos.size());
	}

	public OrganizationDTO findOrganizationByUsername(List<OrganizationDTO> organizationDTOs, String username) {
		for (OrganizationDTO organization : organizationDTOs) {
			if (organization.getUsername().equals(username)) {
				return organization;
			}
		}
		return null; // Return null if the organization is not found
	}

	public Page<DataUsageApiResponse> getBackendAPIUsage(String owner, Integer year, Integer month, String apiId,
			String searchFilter, Pageable pageable) {
		Page<DataUsageApiResponse> dataUsageApiResponsePage = amApiRepository
				.findByOwnerAndYearAndMonthAndApiIdAndSearchFilter(owner, year, month, apiId, searchFilter, pageable);

		for (DataUsageApiResponse dataUsageApiResponse : dataUsageApiResponsePage.getContent()) {
			List<RequestCountDTO> requestCountDTOList = dataUsageApiRepository
					.countRequestByResource(dataUsageApiResponse.getApiId());
			dataUsageApiResponse.setDetails(requestCountDTOList);
		}

		return dataUsageApiResponsePage;
	}

	public Page<RequestCountDTO> getBackendAPIUsageDetails(String apiId, Pageable pageable) {

		Page<RequestCountDTO> requestCountDTOList = dataUsageApiRepository.countRequestByResource(apiId, pageable);

		return requestCountDTOList;
	}

	public Map<String, Object> totalMonthlyDetailLog(String owner, String applicationId, String apiId,
			String searchFilter, Integer year, Integer month,Boolean showDeleted) {
		return dataUsageApiRepository.totalMonthlyDetailLog(owner, applicationId, apiId, searchFilter, year, month,showDeleted);
	}

	public Page<?> getErrorSummary(String apiId, String version, boolean asPercent, String search, Pageable pageable) {
		Page<ErrorSummary> apiUsagePage = dataUsageApiRepository.getAPIUsageByFilters(apiId, version, search, pageable);
		List<Map<String, Object>> errorSummaryList = new ArrayList<>();
		if (asPercent) {
			apiUsagePage.getContent().forEach(errorSummary -> {
				Map<String, Object> errorSummaryMap = convertErrorSummaryToMap(errorSummary);
				errorSummaryList.add(errorSummaryMap);
			});

			return new PageImpl<>(errorSummaryList, pageable, apiUsagePage.getTotalElements());
		}

		return apiUsagePage;
	}

	private LinkedHashMap<String, Object> convertErrorSummaryToMap(ErrorSummary errorSummary) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		Long totalCount = errorSummary.getTotalCount();
		if (totalCount > 0) {
			map.put("apiId", errorSummary.getApiId());
			map.put("apiName", errorSummary.getApiName());
			map.put("apiResourceTemplate", errorSummary.getApiResourceTemplate());
			map.put("apiMethod", errorSummary.getApiMethod());
			map.put("count1xx", errorSummary.getCount1xx() * 100.0 / totalCount);
			map.put("count2xx", errorSummary.getCount2xx() * 100.0 / totalCount);
			map.put("count3xx", errorSummary.getCount3xx() * 100.0 / totalCount);
			map.put("count4xx", errorSummary.getCount4xx() * 100.0 / totalCount);
			map.put("count5xx", errorSummary.getCount5xx() * 100.0 / totalCount);
			map.put("totalCount", errorSummary.getTotalCount());
		}
		return map;
	}

}
