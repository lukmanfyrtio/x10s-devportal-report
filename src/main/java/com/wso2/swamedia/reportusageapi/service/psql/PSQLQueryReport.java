package com.wso2.swamedia.reportusageapi.service.psql;

import java.time.LocalDate;

public class PSQLQueryReport {

	public static String getSubscriptionsRemaining(String dbUserSchema, String dbBillingSchema) {
		String query = "SELECT attr.UM_ATTR_VALUE as organizationName, uu.UM_USER_NAME, subs.subs_state_id , AM_SUBSCRIPTION.SUBSCRIPTION_ID, AM_APPLICATION.NAME AS APPLICATION_NAME, AM_API.API_NAME  AS API_NAME, AM_POLICY_SUBSCRIPTION.NAME AS POLICY_NAME, subs.quota AS INIT_QUOTA, AM_POLICY_SUBSCRIPTION.CUSTOM_ATTRIBUTES,COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS API_USAGE, COALESCE(subs.quota, 0) - COALESCE(DATA_USAGE.USAGE_COUNT, 0) AS REMAINING_QUOTA, AM_SUBSCRIPTION.CREATED_TIME AS START_DATE, (EXTRACT(EPOCH FROM (AM_SUBSCRIPTION.CREATED_TIME + INTERVAL '30 DAY')) - EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)) / 86400 AS REMAINING_DAYS, AM_POLICY_SUBSCRIPTION.TIME_UNIT AS TIME_UNIT, AM_POLICY_SUBSCRIPTION.UNIT_TIME AS UNIT_TIME,subs.notes FROM "
				+ dbBillingSchema
				+ ".subscription subs LEFT JOIN AM_SUBSCRIPTION  ON  subs.subscription_id = AM_SUBSCRIPTION.UUID LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID  = AM_SUBSCRIBER.SUBSCRIBER_ID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME LEFT JOIN " + dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID  = AM_API.API_ID LEFT JOIN AM_POLICY_SUBSCRIPTION ON AM_SUBSCRIPTION.TIER_ID = AM_POLICY_SUBSCRIPTION.NAME LEFT JOIN ( SELECT \n\t\tDATA_USAGE_API.SUBSCRIPTION_UUID ,\n\t\tDATA_USAGE_API.API_ID,\n\t\tDATA_USAGE_API.APPLICATION_ID,\n\t\tCOUNT(*) AS USAGE_COUNT ,\n\t\tAPPLICATION_OWNER\n\tFROM\n\t\tDATA_USAGE_API\n\tLEFT JOIN "
				+ dbBillingSchema
				+ ".subscription subs on\n\t\tsubs.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID\n\tWHERE\n\t\tDATA_USAGE_API.REQUEST_TIMESTAMP BETWEEN subs.start_date AND subs.end_date AND DATA_USAGE_API.KEY_TYPE = 'PRODUCTION' AND DATA_USAGE_API.PROXY_RESPONSE_CODE BETWEEN 200 AND 299 \n\tGROUP BY\n\t\tSUBSCRIPTION_UUID,\n\t\tDATA_USAGE_API.API_ID,\n\t\tAPPLICATION_ID ,\n\t\tAPPLICATION_OWNER  ) AS DATA_USAGE ON subs.subscription_id  = DATA_USAGE.SUBSCRIPTION_UUID WHERE AM_POLICY_SUBSCRIPTION.BILLING_PLAN != 'FREE' AND (:owner::text IS NULL OR AM_SUBSCRIBER.USER_ID = :owner) AND (:owner::text IS NULL OR subs.is_active = true ) ORDER BY REMAINING_DAYS ASC, REMAINING_QUOTA DESC, API_USAGE DESC;";
		return query;
	}

	public static String getMonthlyTotalRowByGroupByWithSearchAndPageable(String dbUserSchema, String dbBillingSchema) {
		String sql = "SELECT DATA_USAGE_API.API_NAME, DATA_USAGE_API.API_VERSION, APPLICATION_OWNER, DATA_USAGE_API.API_ID, APPLICATION_NAME, COUNT(*) AS total_row_count, APPLICATION_ID, attr.UM_ATTR_VALUE FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema + ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME LEFT JOIN "
				+ dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND (:apiId::text IS NULL OR DATA_USAGE_API.API_ID = :apiId) AND (:applicationId::text IS NULL OR DATA_USAGE_API.APPLICATION_ID = :applicationId) AND (:organization::text IS NULL OR attr.UM_ATTR_VALUE = :organization) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') AND (:search::text IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search::text, '%')) OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :search::text, '%'))) AND DATA_USAGE_API.KEY_TYPE = :keyType GROUP BY DATA_USAGE_API.APPLICATION_ID, API_NAME, API_VERSION, DATA_USAGE_API.APPLICATION_OWNER, DATA_USAGE_API.API_ID, APPLICATION_NAME, attr.UM_ATTR_VALUE ORDER BY API_ID, APPLICATION_NAME";
		return sql;
	}

	public static String getTotalApisAndRequestsByOwnerAndFilters(String dbUserSchema, String dbBillingSchema) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT COUNT(DISTINCT DATA_USAGE_API.API_ID) AS total_apis, ")
				.append("COUNT(*) AS total_request, ").append("COUNT(DISTINCT attr.UM_ATTR_VALUE) AS total_customer ")
				.append("FROM DATA_USAGE_API ")
				.append("LEFT JOIN " + dbBillingSchema
						+ ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID ")

				.append("LEFT JOIN " + dbUserSchema
						+ ".UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME ")

				.append("LEFT JOIN " + dbUserSchema + ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID ")
				.append("AND attr.UM_ATTR_NAME = 'organizationName' ")
				.append("WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) ")
				.append("AND (:showDeleted = true OR s.is_active = true) ")
				.append("AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) ")
				.append("AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) ")
				.append("AND (:apiId::text IS NULL OR DATA_USAGE_API.API_ID = :apiId) ")
				.append("AND (:applicationId::text IS NULL OR APPLICATION_ID = :applicationId) ")
				.append("AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') ")
				.append("AND (:organization::text IS NULL OR attr.UM_ATTR_VALUE = :organization) ")
				.append("AND DATA_USAGE_API.KEY_TYPE = :keyType ");
		return sqlQuery.toString();
	}

	public static String getApiUsageByApi(LocalDate startDate, LocalDate endDate) {
		String query = "WITH TotalDataUsage AS (   SELECT COUNT(*) AS total_count FROM DATA_USAGE_API    WHERE 1=1 AND (APPLICATION_OWNER = :owner OR :owner::text IS NULL)    AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") SELECT API_ID, API_NAME, COUNT(*) AS row_count, (COUNT(*) / (SELECT total_count FROM TotalDataUsage)) * 100 AS percentage FROM DATA_USAGE_API WHERE 1=1 AND (APPLICATION_OWNER = :owner OR :owner::text IS NULL) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + "GROUP BY API_ID, API_NAME";
		return query;
	}

	public static String getApiUsageByResponseCode(LocalDate startDate, LocalDate endDate) {
		String query = "SELECT PROXY_RESPONSE_CODE, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1 AND (:owner::text IS NULL OR APPLICATION_OWNER = :owner ) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1 AND (:owner::text IS NULL OR APPLICATION_OWNER = :owner ) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY PROXY_RESPONSE_CODE";
		return query;
	}

	public static String getApiUsageByApplication(LocalDate startDate, LocalDate endDate) {
		String query = "SELECT APPLICATION_ID, APPLICATION_NAME, COUNT(*) AS row_count, (COUNT(*) / (SELECT COUNT(*) FROM DATA_USAGE_API WHERE 1=1 AND (:owner::text IS NULL OR APPLICATION_OWNER = :owner ) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate)
				+ ") * 100) AS percentage FROM DATA_USAGE_API WHERE 1=1 AND (:owner::text IS NULL OR APPLICATION_OWNER = :owner ) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
				+ getOptionalDateRangeCondition(startDate, endDate) + " GROUP BY APPLICATION_ID, APPLICATION_NAME";
		return query;
	}

	public static String fetchMonthlyDetailLogData(String dbUserSchema, String dbBillingSchema) {
		String baseSql = "SELECT  TO_CHAR(REQUEST_TIMESTAMP, 'YYYY-Mon-DD HH:MI:SS') AS requestTimestamp, CONCAT(API_METHOD, ' ', API_RESOURCE_TEMPLATE) AS resource, PROXY_RESPONSE_CODE, DATA_USAGE_API.API_ID, APPLICATION_ID, API_NAME, APPLICATION_NAME, attr.UM_ATTR_VALUE as organization FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema + ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME LEFT JOIN "
				+ dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND APPLICATION_ID = :applicationId AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND DATA_USAGE_API.KEY_TYPE = :keyType AND DATA_USAGE_API.API_ID = :apiId AND (:searchFilter::text IS NULL OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter::text , '%')) OR PROXY_RESPONSE_CODE::text LIKE LOWER(CONCAT('%', :searchFilter::text , '%')))) ";
		return baseSql;
	}

	public static String getResourceSumTotalData(String dbUserSchema, String dbBillingSchema) {
		String sql = "SELECT COUNT(DISTINCT DATA_USAGE_API.API_ID) AS total_apis, COUNT(*) AS total_request FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema
				+ ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND (:apiId::text IS NULL OR DATA_USAGE_API.API_ID = :apiId) AND DATA_USAGE_API.KEY_TYPE = :keyType AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND (:resource::text IS NULL OR API_RESOURCE_TEMPLATE = :resource)";
		return sql;
	}

	private static String getOptionalDateRangeCondition(LocalDate startDate, LocalDate endDate) {
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

	public static String getMonth() {
		String query = "SELECT DISTINCT EXTRACT(MONTH FROM REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND  (:year::int IS NULL OR  EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year ) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') ";
		return query;
	}

	public static String getApiResourceByAPI() {
		String query = "SELECT DISTINCT API_RESOURCE_TEMPLATE  FROM DATA_USAGE_API WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:apiId::text IS NULL OR API_ID = :apiId) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') AND API_RESOURCE_TEMPLATE IS NOT NULL";
		return query;
	}

	public static String getOrganizations() {
		String sql = "SELECT U.UM_USER_NAME as username ,UA.UM_ATTR_NAME, UA.UM_ATTR_VALUE as value\nFROM UM_USER U\nJOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID\nWHERE  UM_ATTR_NAME ='organizationName'";
		return sql;
	}

	public static String totalMonthlyDetailLog(String dbUserSchema, String dbBillingSchema) {
		String sql = "SELECT COUNT(*) as request_count, COUNT(CASE WHEN PROXY_RESPONSE_CODE NOT BETWEEN 200 AND 299 THEN 1 END) AS count_not_200, COUNT(CASE WHEN PROXY_RESPONSE_CODE = 200 THEN 1 END) AS count_200 FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema
				+ ".subscription s on s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND APPLICATION_ID = :applicationId AND DATA_USAGE_API.API_ID = :apiId AND DATA_USAGE_API.KEY_TYPE = :keyType AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') AND (:searchFilter::text IS NULL OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter::text, '%'))  OR PROXY_RESPONSE_CODE::text LIKE LOWER(CONCAT('%', :searchFilter::text , '%'))))";
		return sql;
	}

	public static String getResourceSumListDataBaseSql(String dbUserSchema, String dbBillingSchema) {
		String baseSql = "SELECT API_NAME, API_VERSION, API_RESOURCE_TEMPLATE, API_METHOD, COUNT(*) AS request_count, DATA_USAGE_API.API_ID FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema
				+ ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND (:apiId::text IS NULL OR DATA_USAGE_API.API_ID = :apiId) AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND (:resource::text IS NULL OR API_RESOURCE_TEMPLATE = :resource) AND DATA_USAGE_API.KEY_TYPE = :keyType AND (:search::text IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search::text, '%')) OR LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :search::text, '%'))) ";
		return baseSql;
	}

	public static String getResourceSumListDataCounteSql(String dbUserSchema, String dbBillingSchema) {
		String countSql = "SELECT COUNT(DISTINCT CONCAT(API_NAME, API_VERSION, API_RESOURCE_TEMPLATE, API_METHOD, DATA_USAGE_API.API_ID))  FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema
				+ ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND (:year::int IS NULL OR EXTRACT(YEAR FROM REQUEST_TIMESTAMP) = :year) AND (:month::int IS NULL OR EXTRACT(MONTH FROM REQUEST_TIMESTAMP) = :month) AND (:apiId::text IS NULL OR DATA_USAGE_API.API_ID = :apiId) AND DATA_USAGE_API.KEY_TYPE = :keyType AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND (:resource::text IS NULL OR API_RESOURCE_TEMPLATE = :resource) AND (:search::text IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search::text, '%')) OR LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :search::text, '%'))) ";
		return countSql;
	}

	public static String getDetailLogResourceSumBaseSQl(String dbUserSchema, String dbBillingSchema) {
		String baseSql = "SELECT APPLICATION_NAME, API_NAME, COUNT(*) AS request_count, COUNT(CASE WHEN PROXY_RESPONSE_CODE BETWEEN 200 AND 299 THEN 1 END) AS count_200, COUNT(CASE WHEN PROXY_RESPONSE_CODE NOT BETWEEN 200 AND 299 THEN 1 END) AS count_not_200, DATA_USAGE_API.API_ID, APPLICATION_ID, APPLICATION_OWNER, attr.UM_ATTR_VALUE AS organization FROM DATA_USAGE_API LEFT JOIN "
				+ dbBillingSchema + ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME LEFT JOIN "
				+ dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner::text) AND (:showDeleted = true OR s.is_active = true) AND API_RESOURCE_TEMPLATE = :resource::text AND DATA_USAGE_API.API_ID = :apiId AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND DATA_USAGE_API.KEY_TYPE = :keyType AND (:searchFilter::text IS NULL OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter::text, '%')) OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter::text, '%'))) ";
		return baseSql;
	}

	public static String getDetailLogResourceSumCountSql(String dbUserSchema, String dbBillingSchema) {
		String countSql = "SELECT COUNT(DISTINCT APPLICATION_ID) FROM DATA_USAGE_API LEFT JOIN " + dbBillingSchema
				+ ".subscription s ON s.subscription_id = DATA_USAGE_API.SUBSCRIPTION_UUID WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND (:showDeleted = true OR s.is_active = true) AND API_RESOURCE_TEMPLATE = :resource::text AND DATA_USAGE_API.API_ID = :apiId::text AND APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') AND DATA_USAGE_API.KEY_TYPE = :keyType AND (:searchFilter::text IS NULL OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter::text, '%')) OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter::text, '%'))) ";
		return countSql;
	}

	public static String getCustomers(String dbUserSchema, String dbBillingSchema) {
		String sqlQuery = "SELECT uu.*, attr.UM_ATTR_VALUE AS organizationName FROM AM_SUBSCRIBER as2 JOIN "
				+ dbUserSchema + ".UM_USER uu ON as2.USER_ID = uu.UM_USER_NAME LEFT JOIN " + dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName'";
		return sqlQuery;
	}

	public static String getTotalCustomers(String dbUserSchema, String dbBillingSchema) {
		String sqlQuery = "select COUNT(DISTINCT UM_ATTR_VALUE)from " + dbUserSchema
				+ ".UM_USER_ATTRIBUTE where UM_ATTR_NAME='organizationName'";
		return sqlQuery;
	}

	public static String getYears() {
		String query = "SELECT DISTINCT EXTRACT(YEAR FROM REQUEST_TIMESTAMP) AS year FROM DATA_USAGE_API WHERE (:owner::text IS NULL OR APPLICATION_OWNER = :owner) AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') ";
		return query;
	}

	public static String getApis(String dbUserSchema, String dbBillingSchema) {
		String query = "SELECT DISTINCT AM_API.API_ID, AM_API.API_NAME,AM_API.API_UUID FROM AM_SUBSCRIPTION LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID = AM_API.API_ID LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME LEFT JOIN " + dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' WHERE (:owner::text IS NULL OR AM_SUBSCRIBER.USER_ID  = :owner) AND (:organizationName::text IS NULL OR attr.UM_ATTR_VALUE = :organizationName)";
		return query;
	}

	public static String getApiNameAndId(String dbUserSchema, String dbBillingSchema) {
		String query = "SELECT DISTINCT AM_API.API_ID, AM_API.API_NAME,AM_API.API_UUID,AM_SUBSCRIBER.USER_ID  FROM AM_SUBSCRIPTION LEFT JOIN AM_API ON AM_SUBSCRIPTION.API_ID = AM_API.API_ID LEFT JOIN AM_APPLICATION ON AM_SUBSCRIPTION.APPLICATION_ID = AM_APPLICATION.APPLICATION_ID LEFT JOIN AM_SUBSCRIBER ON AM_APPLICATION.SUBSCRIBER_ID = AM_SUBSCRIBER.SUBSCRIBER_ID LEFT JOIN "
				+ dbUserSchema + ".UM_USER uu ON AM_SUBSCRIBER.USER_ID = uu.UM_USER_NAME LEFT JOIN " + dbUserSchema
				+ ".UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID AND attr.UM_ATTR_NAME = 'organizationName' WHERE (:owner::text IS NULL OR AM_SUBSCRIBER.USER_ID  = :owner) AND (:organizationName::text IS NULL OR attr.UM_ATTR_VALUE = :organizationName)";
		return query;
	}
}
