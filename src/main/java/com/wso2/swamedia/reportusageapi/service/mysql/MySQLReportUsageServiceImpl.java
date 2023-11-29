package com.wso2.swamedia.reportusageapi.service.mysql;

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
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.wso2.swamedia.reportusageapi.repo.mysql.AmApiRepositoryMySQL;
import com.wso2.swamedia.reportusageapi.repo.mysql.DataUsageApiRepositoryMySQL;
import com.wso2.swamedia.reportusageapi.service.ReportUsageService;
import com.wso2.swamedia.reportusageapi.utils.DatabaseUtilsUser;
@Service
@Profile("mysql")
public class MySQLReportUsageServiceImpl implements ReportUsageService{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportUsageService.class);

	@Value("${spring.billing-datasource.url}")
	private String databaseUrl;

	public String getBillingSchema() {
		// Extract database name from URL
		String[] urlParts = databaseUrl.split("/");
		String databaseName = urlParts[urlParts.length - 1];
		return databaseName;
	}

	@Autowired
	private DataUsageApiRepositoryMySQL dataUsageApiRepository;

	@Autowired
	private AmApiRepositoryMySQL amApiRepository;

	@Autowired
	private DatabaseUtilsUser dbUtilsUser;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public MonthlySummary getMonthlyReport(Integer year, Integer month, String applicationId, String apiId,
			String organizationToken, int page, int size, String search, String organization, Boolean showDeleted,String keyType)
			throws Exception {
		LOGGER.info("Retrieving monthly report for year: {}, month: {}, organizationToken: {}", year, month, organizationToken);

		MonthlySummary monthlySummary = new MonthlySummary();

		try {
			Map<String, Object> dataTotal = getTotalApisAndRequestsByOwnerAndFilters(organizationToken, year, month, apiId,
					applicationId, organization, showDeleted,keyType);
			monthlySummary.setTotalApis(Integer.parseInt(dataTotal.get("total_apis").toString()));
			monthlySummary.setRequestCount(Integer.parseInt(dataTotal.get("total_request").toString()));
			monthlySummary.setTotalCustomers(Integer.parseInt(dataTotal.get("total_customer").toString()));
		} catch (Exception e) {
			LOGGER.error("Error retrieving total APIs and requests: {}", e.getMessage());
			throw e; // Rethrow the exception to the caller
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<MonthlySummary.ApiDetails> result = getMonthlyTotalRowByGroupByWithSearchAndPageable(organizationToken, year,
					month, apiId, showDeleted, applicationId, search, organization, pageable,keyType);

			monthlySummary.setDetails(result);
		} catch (Exception e) {
			LOGGER.error("Error retrieving monthly report details: {}", e.getMessage());
			throw e; // Rethrow the exception to the caller
		}

		LOGGER.info("Monthly report retrieval completed");

		return monthlySummary;
	}

	public Page<MonthlySummaryDetails> getMonthlyDetailLogReport(String owner, String applicationId, String apiId,
			String searchFilter, Pageable pageable, Integer year, Integer month, Boolean showDeletedSubscription,String keyType) {
		LOGGER.info("Retrieving API Monthly detail log report for owner: {}, applicationId: {}, apiId: {}", owner,
				applicationId, apiId);

		try {
			Page<MonthlySummaryDetails> monthlyDetailLogPage = fetchMonthlyDetailLogData(pageable, owner, applicationId,
					apiId, searchFilter, year, month, showDeletedSubscription,keyType);

			LOGGER.info("API Monthly detail log report retrieval completed");
			return monthlyDetailLogPage;
		} catch (Exception e) {
			String errorMessage = "Error retrieving API monthly detail log report: " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage, e); // Rethrow a runtime exception to indicate unexpected errors
		}
	}

	@Override
	public ResourceSummary getResourceReport(Integer year, Integer month, String resource, String apiId,
			String organization, int page, int size, String search, Boolean showDeletedSubscription,String keyType) {
		LOGGER.info("Retrieving resource summary for year: {}, month: {}, resource: {}, organization: {}", year, month,
				resource, organization);

		ResourceSummary resourceSummary = new ResourceSummary();
		try {
			Map<String, Object> resourceSumTotal = getResourceSumTotalData(organization, year, month, apiId, resource,
					showDeletedSubscription,keyType);
			resourceSummary.setTotalApis(Integer.valueOf(resourceSumTotal.get("total_apis").toString()));
			resourceSummary.setRequestCount(Integer.valueOf(resourceSumTotal.get("total_request").toString()));
		} catch (Exception e) {
			LOGGER.error("Error retrieving total APIs and requests for resource: {}", e.getMessage());
			throw new RuntimeException("Error retrieving total APIs and requests for resource", e);
		}

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<ResourceSummary.ApiDetails> resourceSummaryPage = getResourceSumListData(organization, year, month, apiId,
					resource, search, pageable, showDeletedSubscription,keyType);
			resourceSummary.setDetails(resourceSummaryPage);
		} catch (Exception e) {
			LOGGER.error("Error retrieving resource summary details: {}", e.getMessage());
			throw new RuntimeException("Error retrieving resource summary details", e);
		}

		LOGGER.info("Resource summary retrieval completed");

		return resourceSummary;
	}

	public Page<ResourceSummaryDetails> getDetailLogResourceSum(String organization, String resource, String apiId,
			String searchFilter, Pageable pageable, Boolean showDeletedSubscription,String keyType) throws Exception {
		LOGGER.info("Retrieving resource detail log for owner: {}, resource: {}, apiId: {}", organization, resource, apiId);

		Page<ResourceSummaryDetails> pageM = null;
		try {

			pageM = getDetailLogResourceSum(pageable, organization, resource, apiId, searchFilter, showDeletedSubscription, keyType);
		} catch (Exception e) {
			String error = String.format("Error retrieving resource detail log: {}", e.getMessage());
			LOGGER.error(error);
			throw new Exception(e.getMessage());
		}

		LOGGER.info("Resource detail log retrieval completed");

		return pageM;
	}

	public List<DashboardPercentageDTO> getApiUsageByApi(LocalDate startDate, LocalDate endDate, String username) {
		String query = MySQLQueryReport.getApiUsageByApi(startDate, endDate);

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);

		LOGGER.info("SQL Query: {}", query); // Use parameterized logging

		return namedParameterJdbcTemplate.query(query, parameters, new DashboardApiPercentageMapper());
	}

	public List<LinkedHashMap<String, Object>> getPlanByPaymentType(Integer subsTypeId, Boolean isDeployed) {
		String sqlQuery = "SELECT * FROM AM_POLICY_SUBSCRIPTION ";
		int val = (isDeployed) ? 1 : 0;
		if (subsTypeId == 2) {
			sqlQuery += "WHERE CUSTOM_ATTRIBUTES = '[{\"name\":\"type_subscription\",\"value\":\"time\"}]' AND IS_DEPLOYED = "
					+ val;
		} else {
			sqlQuery += "WHERE IS_DEPLOYED = " + val;
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

		String query = MySQLQueryReport.getApiUsageByApplication(startDate, endDate);
		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardAppPercentageMapper());
	}

	public List<DashboardPercentageDTO> getApiUsageByResponseCode(LocalDate startDate, LocalDate endDate,
			String username) {
		String query = MySQLQueryReport.getApiUsageByResponseCode(startDate, endDate);

		Map<String, Object> params = getOptionalDateRangeNamedParams(startDate, endDate);
		params.put("owner", username);
		MapSqlParameterSource parameters = new MapSqlParameterSource(params);
		LOGGER.info(query);
		return namedParameterJdbcTemplate.query(query, parameters, new DashboardResCodePercentageMapper());
	}

	public Map<String, Object> getOptionalDateRangeNamedParams(LocalDate startDate, LocalDate endDate) {
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
		String query = MySQLQueryReport.getApiNameAndId(dbUtilsUser.getSchemaName(), getBillingSchema());

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
		String query = MySQLQueryReport.getApis(dbUtilsUser.getSchemaName(), getBillingSchema());

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
		String query = MySQLQueryReport.getYears();

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
		String sqlQuery = MySQLQueryReport.getCustomers(dbUtilsUser.getSchemaName(), getBillingSchema());

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
		String sqlQuery = MySQLQueryReport.getTotalCustomers(dbUtilsUser.getSchemaName(), getBillingSchema());
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
		String sqlQuery = "select DISTINCT UM_ATTR_VALUE as organizationName " 
				+ "from " + dbUtilsUser.getSchemaName()
				+ ".UM_USER_ATTRIBUTE " + "where UM_ATTR_NAME='organizationName'";

		MapSqlParameterSource parameters = new MapSqlParameterSource();
//		parameters.addValue("owner", owner);

		return namedParameterJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) -> {
			Map<String, Object> customers = new HashMap<>();
			customers.put("organizationName", rs.getString("organizationName"));
			return customers;
		});
	}

	public List<Map<String, Object>> getMonth(String owner, int year) {

		String query = MySQLQueryReport.getMonth();
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
		String query = MySQLQueryReport.getApiResourceByAPI();
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
		String sql = MySQLQueryReport.getOrganizations();

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
		String query = MySQLQueryReport.getSubscriptionsRemaining(dbUtilsUser.getSchemaName(), getBillingSchema());

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("owner", owner);
		List<TableRemainingDayQuota> dtos = namedParameterJdbcTemplate.query(query, parameters, (resultSet, rowNum) -> {
			TableRemainingDayQuota dto = new TableRemainingDayQuota();
			String subsStateId = resultSet.getString("subs_state_id");
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
						dto.setRemaining(
								resultSet.getInt("REMAINING_QUOTA") < 0 ? 0 : resultSet.getInt("REMAINING_QUOTA"));
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
			String searchFilter, Pageable pageable, String keyType) {
		Page<DataUsageApiResponse> dataUsageApiResponsePage = amApiRepository
				.findByOwnerAndYearAndMonthAndApiIdAndSearchFilter(owner, year, month, apiId, searchFilter, keyType,pageable);

		for (DataUsageApiResponse dataUsageApiResponse : dataUsageApiResponsePage.getContent()) {
			List<RequestCountDTO> requestCountDTOList = dataUsageApiRepository
					.countRequestByResource(dataUsageApiResponse.getApiId(),keyType);
			dataUsageApiResponse.setDetails(requestCountDTOList);
		}

		return dataUsageApiResponsePage;
	}

	public Page<RequestCountDTO> getBackendAPIUsageDetails(String apiId, Pageable pageable, String keyType) {

		Page<RequestCountDTO> requestCountDTOList = dataUsageApiRepository.countRequestByResource(apiId,pageable, keyType);

		return requestCountDTOList;
	}

	public Page<?> getErrorSummary(String apiId, String version, boolean asPercent, String search, Pageable pageable,String keyType) {
		Page<ErrorSummary> apiUsagePage = dataUsageApiRepository.getAPIUsageByFilters(apiId, version, search, keyType, pageable);
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

	public LinkedHashMap<String, Object> convertErrorSummaryToMap(ErrorSummary errorSummary) {
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

	public Page<MonthlySummary.ApiDetails> getMonthlyTotalRowByGroupByWithSearchAndPageable(String organizationToken, Integer year,
			Integer month, String apiId, Boolean showDeleted, String applicationId, String search, String organization,
			Pageable pageable,String keyType) {

		String sql = MySQLQueryReport.getMonthlyTotalRowByGroupByWithSearchAndPageable(dbUtilsUser.getSchemaName(),
				getBillingSchema());

		// Create the parameters to be used in the query
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("organizationToken", organizationToken);
		params.addValue("year", year);
		params.addValue("month", month);
		params.addValue("apiId", apiId);
		params.addValue("showDeleted", showDeleted);
		params.addValue("applicationId", applicationId);
		params.addValue("search", search);
		params.addValue("organization", organization);
		params.addValue("keyType", keyType);

		// Implement pagination using LIMIT and OFFSET clauses
		long totalRowCount = namedParameterJdbcTemplate.queryForObject(buildCountQuery(sql), params, Long.class);
		int pageSize = pageable.getPageSize();
		int pageNumber = pageable.getPageNumber();
		int offset = pageNumber * pageSize;
		sql = sql + " LIMIT " + pageSize + " OFFSET " + offset;

		try {
			// Execute the query and map the results to ApiDetails objects
			List<MonthlySummary.ApiDetails> results = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
				MonthlySummary.ApiDetails apiDetails = new MonthlySummary.ApiDetails();
				apiDetails.setApiName(rs.getString("API_NAME"));
				apiDetails.setApiId(rs.getString("API_ID"));
				apiDetails.setApiVersion(rs.getString("API_VERSION"));
				apiDetails.setApplicationName(rs.getString("APPLICATION_NAME"));
				apiDetails.setApplicationOwner(rs.getString("APPLICATION_OWNER"));
				apiDetails.setRequestCount(BigInteger.valueOf(rs.getLong("total_row_count")));
				apiDetails.setApplicationId(rs.getString("APPLICATION_ID"));
				apiDetails.setOrganization(rs.getString("UM_ATTR_VALUE"));
				return apiDetails;
			});

			// Return the results as a Page
			return new PageImpl<>(results, pageable, totalRowCount);
		} catch (Exception e) {
			// Handle any exceptions that might occur during the query execution
			e.printStackTrace(); // Or log the error
			return new PageImpl<>(List.of(), pageable, 0); // Return an empty page if there's an error
		}
	}

	public Map<String, Object> getTotalApisAndRequestsByOwnerAndFilters(String organizationToken, Integer year, Integer month,
			String apiId, String applicationId, String organization, Boolean showDeleted,String keyType) {
		String sqlQuery = MySQLQueryReport.getTotalApisAndRequestsByOwnerAndFilters(dbUtilsUser.getSchemaName(),
				getBillingSchema());

		Map<String, Object> params = new HashMap<>();
		params.put("organizationToken", organizationToken);
		params.put("showDeleted", showDeleted);
		params.put("year", year);
		params.put("month", month);
		params.put("apiId", apiId);
		params.put("applicationId", applicationId);
		params.put("organization", organization);
		params.put("keyType", keyType);

		return namedParameterJdbcTemplate.queryForMap(sqlQuery.toString(), params);
	}

	public Map<String, Object> totalMonthlyDetailLog(String organization, String applicationId, String apiId,
			String searchFilter, Integer year, Integer month, Boolean showDeleted,String keyType) {
		String sql = MySQLQueryReport.totalMonthlyDetailLog(dbUtilsUser.getSchemaName(), getBillingSchema());

		Map<String, Object> params = new HashMap<>();
		params.put("organization", organization);
		params.put("applicationId", applicationId);
		params.put("apiId", apiId);
		params.put("searchFilter", searchFilter);
		params.put("year", year);
		params.put("month", month);
		params.put("showDeleted", showDeleted);
		params.put("keyType", keyType);

		return namedParameterJdbcTemplate.queryForMap(sql, params);
	}

	public Page<MonthlySummaryDetails> fetchMonthlyDetailLogData(Pageable pageable, String organization, String applicationId,
			String apiId, String searchFilter, Integer year, Integer month, Boolean showDeleted,String keyType) {
		String baseSql = MySQLQueryReport.fetchMonthlyDetailLogData(dbUtilsUser.getSchemaName(), getBillingSchema());
		String countSql = "SELECT COUNT(*) " + baseSql.substring(baseSql.indexOf("FROM"));

		Map<String, Object> params = new HashMap<>();
		params.put("organization",organization);
		params.put("applicationId", applicationId);
		params.put("apiId", apiId);
		params.put("searchFilter", searchFilter);
		params.put("year", year);
		params.put("month", month);
		params.put("showDeleted", showDeleted);
		params.put("keyType", keyType);

// Get total count for pagination
		long totalRowCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);

// Implement pagination using LIMIT and OFFSET clauses
		int pageSize = pageable.getPageSize();
		int pageNumber = pageable.getPageNumber();
		int offset = pageNumber * pageSize;
		String sql = baseSql + "ORDER BY REQUEST_TIMESTAMP LIMIT " + pageSize + " OFFSET " + offset;

// Execute the query and map the results to MonthlySummaryDetails objects
		List<MonthlySummaryDetails> results = namedParameterJdbcTemplate.query(sql, params,
				BeanPropertyRowMapper.newInstance(MonthlySummaryDetails.class));

// Return the results as a Page
		return new PageImpl<>(results, pageable, totalRowCount);
	}

	public Map<String, Object> getResourceSumTotalData(String organization, Integer year, Integer month, String apiId,
			String resource, Boolean showDeleted,String keyType) {
		String sql = MySQLQueryReport.getResourceSumTotalData(dbUtilsUser.getSchemaName(), getBillingSchema());

		Map<String, Object> params = new HashMap<>();
		params.put("organization", organization);
		params.put("year", year);
		params.put("month", month);
		params.put("apiId", apiId);
		params.put("resource", resource);
		params.put("showDeleted", showDeleted);
		params.put("keyType", keyType);

		return namedParameterJdbcTemplate.queryForMap(sql, params);
	}

	public Page<ResourceSummary.ApiDetails> getResourceSumListData(String organization, Integer year, Integer month,
			String apiId, String resource, String search, Pageable pageable, Boolean showDeleted,String keyType) {
		String baseSql = MySQLQueryReport.getResourceSumListDataBaseSql(dbUtilsUser.getSchemaName(), getBillingSchema());

		String countSql = MySQLQueryReport.getResourceSumListDataCounteSql(dbUtilsUser.getSchemaName(),
				getBillingSchema());

		Map<String, Object> params = new HashMap<>();
		params.put("organization", organization);
		params.put("year", year);
		params.put("month", month);
		params.put("apiId", apiId);
		params.put("resource", resource);
		params.put("search", search);
		params.put("showDeleted", showDeleted);
		params.put("keyType", keyType);

// Get total count for pagination
		long totalRowCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);

// Implement pagination using LIMIT and OFFSET clauses
		int pageSize = pageable.getPageSize();
		int pageNumber = pageable.getPageNumber();
		int offset = pageNumber * pageSize;
		String sql = baseSql
				+ "GROUP BY API_NAME, API_VERSION, API_RESOURCE_TEMPLATE, API_METHOD, DATA_USAGE_API.API_ID "
				+ "ORDER BY request_count DESC LIMIT " + pageSize + " OFFSET " + offset;

// Execute the query and map the results to ResourceSummary.ApiDetails objects
		List<ResourceSummary.ApiDetails> apiDetailsList = namedParameterJdbcTemplate.query(sql, params,
				(rs, rowNum) -> {
					ResourceSummary.ApiDetails apiDetails = new ResourceSummary.ApiDetails();
					apiDetails.setApiName(rs.getString("API_NAME"));
					apiDetails.setApiVersion(rs.getString("API_VERSION"));
					apiDetails.setResource(rs.getString("API_RESOURCE_TEMPLATE"));
					apiDetails.setApiMethod(rs.getString("API_METHOD"));
					apiDetails.setRequestCount(BigInteger.valueOf(rs.getLong("request_count")));
					apiDetails.setApiId(rs.getString("API_ID"));
					return apiDetails;
				});

// Return the results as a Page
		return new PageImpl<>(apiDetailsList, pageable, totalRowCount);
	}

	public Page<ResourceSummaryDetails> getDetailLogResourceSum(Pageable pageable, String organization, String resource,
			String apiId, String searchFilter, Boolean showDeleted,String keyType) {
		String baseSql = MySQLQueryReport.getDetailLogResourceSumBaseSQl(dbUtilsUser.getSchemaName(), getBillingSchema());

		String countSql = MySQLQueryReport.getDetailLogResourceSumCountSql(dbUtilsUser.getSchemaName(),
				getBillingSchema());

		Map<String, Object> params = new HashMap<>();
		params.put("organization", organization);
		params.put("resource", resource);
		params.put("apiId", apiId);
		params.put("searchFilter", searchFilter);
		params.put("showDeleted", showDeleted);
		params.put("keyType", keyType);

// Get total count for pagination
		long totalRowCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);

// Implement pagination using LIMIT and OFFSET clauses
		int pageSize = pageable.getPageSize();
		int pageNumber = pageable.getPageNumber();
		int offset = pageNumber * pageSize;
		String sql = baseSql
				+ "GROUP BY APPLICATION_ID, APPLICATION_NAME, API_NAME, DATA_USAGE_API.API_ID, APPLICATION_OWNER, organization "
				+ "ORDER BY request_count DESC LIMIT " + pageSize + " OFFSET " + offset;

// Execute the query and map the results to ResourceSummaryDetails objects
		List<ResourceSummaryDetails> detailsList = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
			ResourceSummaryDetails details = new ResourceSummaryDetails();
			details.setApplicationId(rs.getString("APPLICATION_ID"));
			details.setApplicationName(rs.getString("APPLICATION_NAME"));
			details.setApiId(rs.getString("API_ID"));
			details.setApiName(rs.getString("API_NAME"));
			details.setRequestCount(BigInteger.valueOf(rs.getLong("request_count")));
			details.setResponseOK(BigInteger.valueOf(rs.getLong("count_200")));
			details.setResponseNOK(BigInteger.valueOf(rs.getLong("count_not_200")));
			details.setApplicationOwner(rs.getString("APPLICATION_OWNER"));
			details.setOrganization(rs.getString("organization"));
			return details;
		});

// Return the results as a Page
		return new PageImpl<>(detailsList, pageable, totalRowCount);
	}

	// Helper method to build the count query for pagination
	public String buildCountQuery(String originalQuery) {
		return "SELECT COUNT(*) FROM (" + originalQuery + ") AS totalRowCount";
	}
}
