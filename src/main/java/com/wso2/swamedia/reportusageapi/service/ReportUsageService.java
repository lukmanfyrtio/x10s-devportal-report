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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.wso2.swamedia.reportusageapi.dto.MonthlySummary;
import com.wso2.swamedia.reportusageapi.dto.MonthlySummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.OrganizationDTO;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummary;
import com.wso2.swamedia.reportusageapi.dto.ResourceSummaryDetails;
import com.wso2.swamedia.reportusageapi.dto.TableRemainingDayQuota;
import com.wso2.swamedia.reportusageapi.mapper.DashboardApiPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardAppPercentageMapper;
import com.wso2.swamedia.reportusageapi.mapper.DashboardResCodePercentageMapper;
import com.wso2.swamedia.reportusageapi.repo.DataUsageApiRepository;

@Service
public class ReportUsageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageService.class);

	@Autowired
	private DataUsageApiRepository dataUsageApiRepository;

	@Autowired
	private DBUtilsUser dbUtilsUser;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
		List<OrganizationDTO> organizationDTOs = getOrganizations();
		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Object[]> result = dataUsageApiRepository.getMonthlyTotalRowByGroupByWithSearchAndPageable(username,
					year, month, apiId, applicationId, search, pageable);

			Page<MonthlySummary.ApiDetails> pageM = result.map(row -> {
				OrganizationDTO org = findOrganizationByUsername(organizationDTOs, (String) row[2]);
				return new MonthlySummary.ApiDetails((String) row[0], (String) row[3], (String) row[1], (String) row[4],
						(String) row[2], (BigInteger) row[5], (String) row[6], org != null ? org.getValue() : null);
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
		List<OrganizationDTO> organizationDTOs = getOrganizations();
		try {
			Page<Object[]> result = dataUsageApiRepository.getDetailLogResourceSum(pageable, owner, resource, apiId,
					searchFilter);

			pageM = result.map(row -> {
				String appName = (String) row[0];
				String apiName = (String) row[1];
				BigInteger requestCount = (BigInteger) row[2];
				BigInteger countNOK = (BigInteger) row[3];
				BigInteger countOK = (BigInteger) row[4];
				String apiIds = (String) row[5];
				String appId = (String) row[6];
				String appOwner = (String) row[8];
				OrganizationDTO org = findOrganizationByUsername(organizationDTOs, (String) row[8]);
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
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY API_ID, API_NAME";

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardApiPercentageMapper());
	}

	public List<DashboardPercentageDTO> getApiUsageByApplication(LocalDate startDate, LocalDate endDate,
			String username) {
		String query = "SELECT APPLICATION_ID, APPLICATION_NAME, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
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
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1"
				+ " AND (:owner IS NULL OR APPLICATION_OWNER = :owner ) "
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

	public List<Map<String, Object>> getApiNameAndId(String owner,String organization) {
		String query = "SELECT DISTINCT AM_API.API_ID, AM_API.API_NAME,AM_API.API_UUID " +
		        "FROM AM_SUBSCRIPTION " +
		        "LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID = AM_API.API_ID " +
		        "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID " +
		        "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID " +
		        "LEFT JOIN apim_shareddb.UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME " +
		        "LEFT JOIN apim_shareddb.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID " +
		        "AND attr.UM_ATTR_NAME = 'organizationName' " +
		        "WHERE (:owner IS NULL OR uu.UM_USER_ID = :owner) " +
		        "AND (:organizationName IS NULL OR attr.UM_ATTR_VALUE = :organizationName)";


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
		String query = "SELECT DISTINCT YEAR(REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner)";

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
		String sqlQuery = "SELECT uu.*, attr.UM_ATTR_VALUE AS organizationName " + "FROM apim_db.AM_SUBSCRIBER as2 "
				+ "JOIN apim_shareddb.UM_USER uu ON as2.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
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

	public List<Map<String, Object>> getMonth(String owner, int year) {
		String query = "SELECT DISTINCT MONTH(REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API "
				+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner) AND "
				+ " (:year IS NULL OR  YEAR(REQUEST_TIMESTAMP) = :year )";

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
				+ " AND (:apiId IS NULL OR API_ID = :apiId)";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		parameters.addValue("apiId", apiId);

		return namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
			Map<String, Object> apiInfo = new HashMap<>();
			apiInfo.put("resource", rs.getString("API_RESOURCE_TEMPLATE"));
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
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception("Error occurred while retrieving organization details.", e);
		}
		return result;
	}

	public Page<TableRemainingDayQuota> getSubscriptionsRemaining(String owner, Pageable pageable) {
		String query = "SELECT " + "attr.UM_ATTR_VALUE as organizationName, " + "AM_SUBSCRIPTION.SUBSCRIPTION_ID, "
				+ "AM_APPLICATION.NAME AS APPLICATION_NAME, " + "AM_API.API_NAME  AS API_NAME, "
				+ "AM_POLICY_SUBSCRIPTION.NAME AS POLICY_NAME, " + "AM_POLICY_SUBSCRIPTION.QUOTA AS INIT_QUOTA, "
				+ "AM_POLICY_SUBSCRIPTION.CUSTOM_ATTRIBUTES," + "COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS API_USAGE, "
				+ "COALESCE(AM_POLICY_SUBSCRIPTION.QUOTA, 0) - COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS REMAINING_QUOTA, "
				+ "AM_SUBSCRIPTION.CREATED_TIME AS START_DATE, "
				+ "DATEDIFF(DATE_ADD(AM_SUBSCRIPTION.CREATED_TIME, INTERVAL 30 DAY), CURDATE()) AS REMAINING_DAYS "
				+ "FROM " + "AM_SUBSCRIPTION "
				+ "LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID "
				+ "LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID  = AM_SUBSCRIBER.SUBSCRIBER_ID "
				+ "LEFT JOIN apim_shareddb.UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME "
				+ "LEFT JOIN apim_shareddb.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' "
				+ "LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID  = AM_API.API_ID "
				+ "LEFT JOIN AM_POLICY_SUBSCRIPTION ON AM_SUBSCRIPTION.TIER_ID = AM_POLICY_SUBSCRIPTION.NAME "
				+ "LEFT JOIN ( " + "SELECT SUBSCRIPTION_ID, API_ID, APPLICATION_ID, COUNT(*) AS USAGE_COUNT ,APPLICATION_OWNER "
				+ "FROM DATA_USAGE_API " + "GROUP BY SUBSCRIPTION_ID, API_ID, APPLICATION_ID ,APPLICATION_OWNER "
				+ ") AS DATA_USAGE ON AM_SUBSCRIPTION.SUBSCRIPTION_ID = DATA_USAGE.SUBSCRIPTION_ID "
				+ "AND AM_API.API_UUID  = DATA_USAGE.API_ID " + "AND AM_APPLICATION.UUID = DATA_USAGE.APPLICATION_ID "
				+ "WHERE " 
				+ "AM_SUBSCRIPTION.SUBS_CREATE_STATE = 'SUBSCRIBE' "
				+ "AND AM_SUBSCRIPTION.SUB_STATUS = 'UNBLOCKED' "
				+ "AND (:owner IS NULL OR DATA_USAGE.APPLICATION_OWNER = :owner) "
				+ "ORDER BY REMAINING_DAYS ASC, REMAINING_QUOTA DESC, API_USAGE DESC;";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		List<TableRemainingDayQuota> dtos = namedParameterJdbcTemplate.query(query, parameters, (resultSet, rowNum) -> {
			TableRemainingDayQuota dto = new TableRemainingDayQuota();
			// Set values to the dto from the resultSet
			dto.setSubscriptionId(resultSet.getString("SUBSCRIPTION_ID"));
			dto.setApplicationName(resultSet.getString("APPLICATION_NAME"));
			dto.setApiName(resultSet.getString("API_NAME"));
			dto.setPolicyName(resultSet.getString("POLICY_NAME"));
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
						dto.setQuota(30);
						dto.setRemaining(resultSet.getInt("REMAINING_DAYS"));
						dto.setTypeSubscription("time");
					} else {
						dto.setQuota(resultSet.getInt("INIT_QUOTA"));
						dto.setApiUsage(resultSet.getInt("API_USAGE"));
						dto.setRemaining(resultSet.getInt("REMAINING_QUOTA"));
						dto.setTypeSubscription("quota");
					}
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

}
