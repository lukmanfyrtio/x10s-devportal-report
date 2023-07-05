package com.wso2.swamedia.reportusageapi.repo;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wso2.swamedia.reportusageapi.dto.ErrorSummary;
import com.wso2.swamedia.reportusageapi.dto.RequestCountDTO;
import com.wso2.swamedia.reportusageapi.model.DataUsageApi;

@Repository
public interface DataUsageApiRepository extends JpaRepository<DataUsageApi, String> {

	// monthly summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request,COUNT(DISTINCT attr.UM_ATTR_VALUE) AS total_customer "
			+ "FROM DATA_USAGE_API "
			+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME "
			+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId) "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:organization IS NULL OR attr.UM_ATTR_VALUE = :organization) ", nativeQuery = true)
	Map<String, Object> getTotalApisAndRequestsByOwnerAndFilters(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationId") String applicationId,@Param("organization") String organization);

	
	// monthly summary table list
	@Query(value = "SELECT API_NAME,API_VERSION ,APPLICATION_OWNER ,API_ID, APPLICATION_NAME, "
			+ "COUNT(*) AS total_row_count ,APPLICATION_ID ,attr.UM_ATTR_VALUE "
			+ "FROM DATA_USAGE_API " 
			+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME "
			+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId) "
			+ "AND (:organization IS NULL OR attr.UM_ATTR_VALUE = :organization) "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "GROUP BY APPLICATION_ID,API_NAME,API_VERSION "
			+ ",APPLICATION_OWNER ,API_ID, APPLICATION_NAME ,attr.UM_ATTR_VALUE " 
			+ "ORDER "
			+ " BY API_ID, APPLICATION_NAME ", countQuery = "SELECT COUNT(DISTINCT APPLICATION_ID) "
					+ "FROM DATA_USAGE_API " 
					+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME "
					+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
					+ "AND attr.UM_ATTR_NAME = 'organizationName' "
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND (:apiId IS NULL OR API_ID = :apiId) "
					+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId) "
					+ "AND (:organization IS NULL OR attr.UM_ATTR_VALUE = :organization) "
					+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :search, '%'))) ", nativeQuery = true)
	Page<Object[]> getMonthlyTotalRowByGroupByWithSearchAndPageable(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationId") String applicationId, @Param("search") String search,@Param("organization") String organization, Pageable pageable);

	@Query(value = "SELECT DATE_FORMAT(REQUEST_TIMESTAMP, '%Y-%b-%d %H:%i:%s') AS requestTimestamp, "
			+ "CONCAT(API_METHOD, ' ', API_RESOURCE_TEMPLATE) AS resource, "
			+ "PROXY_RESPONSE_CODE, API_ID, APPLICATION_ID ,API_NAME,APPLICATION_NAME ,attr.UM_ATTR_VALUE " 
			+ "FROM DATA_USAGE_API "
			+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME "
			+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" 
			+ "AND APPLICATION_ID = :applicationId "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND API_ID = :apiId " 
			+ "   AND (:searchFilter IS NULL "
			+ "OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
			+ "    OR LOWER(PROXY_RESPONSE_CODE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  )) "
			+ "ORDER BY REQUEST_TIMESTAMP", 
			countQuery = "SELECT COUNT(*) " 
			+ "FROM DATA_USAGE_API "
			+ "LEFT JOIN apim_shareddb_test.UM_USER uu ON DATA_USAGE_API.APPLICATION_OWNER = uu.UM_USER_NAME "
			+ "LEFT JOIN apim_shareddb_test.UM_USER_ATTRIBUTE attr ON uu.UM_ID = attr.UM_USER_ID "
			+ "AND attr.UM_ATTR_NAME = 'organizationName' "
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" 
					+ "AND APPLICATION_ID = :applicationId "
					+ "AND API_ID = :apiId "
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "    AND (:searchFilter IS NULL "
					+ "OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
					+ "    OR LOWER(PROXY_RESPONSE_CODE) LIKE LOWER(CONCAT('%', :searchFilter, '%')) ))", 
					nativeQuery = true)
	Page<Object[]> getMonthlyDetailLog(Pageable pageable, @Param("owner") String owner,
			@Param("applicationId") String applicationId, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter,@Param("year") Integer year, @Param("month") Integer month);
	
	@Query(value = "SELECT COUNT(*) as request_count, "
					+ "COUNT(CASE WHEN PROXY_RESPONSE_CODE NOT BETWEEN 200 AND 299 THEN 1 END) AS count_not_200,COUNT(CASE WHEN PROXY_RESPONSE_CODE = 200 THEN 1 END) AS count_200 " 
			+ "FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" 
			+ "AND APPLICATION_ID = :applicationId "
			+ "AND API_ID = :apiId " 
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "   AND (:searchFilter IS NULL "
			+ "OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
			+ "    OR LOWER(PROXY_RESPONSE_CODE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  )) "
			+ "ORDER BY REQUEST_TIMESTAMP", 
					nativeQuery = true)
	Map<String, Object> totalMonthlyDetailLog(@Param("owner") String owner,
			@Param("applicationId") String applicationId, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter,@Param("year") Integer year, @Param("month") Integer month);

	// resource summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource)", nativeQuery = true)
	Map<String, Object> getResourceSumTotal(@Param("owner") String owner, @Param("year") Integer year,
			@Param("month") Integer month, @Param("apiId") String apiId, @Param("resource") String resource);

	// resource summary table list
	@Query(value = "SELECT API_NAME ,API_VERSION ,API_RESOURCE_TEMPLATE,API_METHOD "
			+ ",COUNT(*) as request_count,API_ID ,APPLICATION_ID,APPLICATION_NAME "
			+ "FROM DATA_USAGE_API " 
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource) "
			+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "GROUP BY API_NAME , API_VERSION , API_RESOURCE_TEMPLATE , API_METHOD,API_ID,APPLICATION_ID,APPLICATION_NAME " 
			+ "ORDER "
			+ " BY request_count DESC", 
			countQuery = "SELECT COUNT(DISTINCT API_RESOURCE_TEMPLATE,API_ID) "
					+ "FROM DATA_USAGE_API " 
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND (:apiId IS NULL OR API_ID = :apiId) "
					+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource) "
					+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :search, '%'))) ", 
					nativeQuery = true)
	Page<Object[]> getResourceSumList(@Param("owner") String owner, @Param("year") Integer year,
			@Param("month") Integer month, @Param("apiId") String apiId, @Param("resource") String resource,
			@Param("search") String search, Pageable pageable);

	@Query(value = "SELECT  APPLICATION_NAME ,API_NAME,COUNT(*) as request_count, "
			+ "COUNT(CASE WHEN PROXY_RESPONSE_CODE NOT BETWEEN 200 AND 299  THEN 1 END) AS count_not_200,COUNT(CASE WHEN PROXY_RESPONSE_CODE BETWEEN 200 AND 299  THEN 1 END) AS count_200,"
			+ "API_ID, APPLICATION_ID ,APPLICATION_OWNER " 
			+ " FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" + "AND API_RESOURCE_TEMPLATE = :resource "
			+ "AND API_ID = :apiId " 
			+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "   AND (:searchFilter IS NULL OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
			+ "    OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  ) " 
			+ "GROUP BY " 
			+ "  APPLICATION_ID ," 
			+ "  APPLICATION_NAME ,"
			+ "  API_NAME, " 
			+ "  API_ID ,"
			+ "APPLICATION_OWNER "
			+ "ORDER BY request_count desc", 
			countQuery = " SELECT COUNT(DISTINCT APPLICATION_ID)"
					+ "FROM DATA_USAGE_API " 
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
					+ "AND API_RESOURCE_TEMPLATE = :resource " + "AND API_ID = :apiId "
					+ "AND APPLICATION_OWNER NOT IN ('anonymous','internal-key-app','UNKNOWN') "
					+ "AND (:searchFilter IS NULL "
					+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
					+ "OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  ) ", 
					nativeQuery = true)
	Page<Object[]> getDetailLogResourceSum(Pageable pageable, @Param("owner") String owner,
			@Param("resource") String resource, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter);
	
	
	
//	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.DataUsageApiResponse(d.apiId, d.apiName, d.apiVersion, a.context, COUNT(d.apiId) AS requestCount) " +
//	        "FROM DataUsageApi d " +
//	        "LEFT JOIN d.amApi a " +
//	        "WHERE (:owner IS NULL OR d.applicationOwner = :owner) " +
//	        "AND (:year IS NULL OR YEAR(d.requestTimestamp) = :year) " +
//	        "AND (:month IS NULL OR MONTH(d.requestTimestamp) = :month) " +
//	        "AND (:apiId IS NULL OR d.apiId = :apiId) " +
//	        "AND (:searchFilter IS NULL OR LOWER(a.context) LIKE LOWER(CONCAT('%', :searchFilter, '%')) " +
//	        "OR LOWER(d.apiName) LIKE LOWER(CONCAT('%', :searchFilter, '%'))) " +
//	        "GROUP BY d.apiId, d.apiName, d.apiVersion, a.context " +
//	        "ORDER BY requestCount DESC")
//	Page<DataUsageApiResponse> findByOwnerAndYearAndMonthAndApiIdAndSearchFilter(
//	        @Param("owner") String owner,
//	        @Param("year") Integer year,
//	        @Param("month") Integer month,
//	        @Param("apiId") String apiId,
//	        @Param("searchFilter") String searchFilter,
//	        Pageable pageable);
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.RequestCountDTO(d.apiResourceTemplate, d.apiMethod, COUNT(d)) " +
	        "FROM DataUsageApi d " +
	        "WHERE d.apiId = :apiId " +
	        "AND applicationOwner NOT IN ('anonymous','internal-key-app','UNKNOWN') "+
	        "GROUP BY d.apiResourceTemplate, d.apiMethod")
	List<RequestCountDTO> countRequestByResource(@Param("apiId") String apiId);
	
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.RequestCountDTO(d.apiResourceTemplate, d.apiMethod, COUNT(d)) " +
	        "FROM DataUsageApi d " +
	        "WHERE d.apiId = :apiId " +
	        "AND applicationOwner NOT IN ('anonymous','internal-key-app','UNKNOWN') "+
	        "GROUP BY d.apiResourceTemplate, d.apiMethod")
	Page<RequestCountDTO> countRequestByResource(@Param("apiId") String apiId,Pageable pageable);
	
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.ErrorSummary(d.apiId,d.apiName,d.apiResourceTemplate, d.apiMethod, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 100 AND 199 THEN 1 ELSE 0 END) AS count1xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 200 AND 299 THEN 1 ELSE 0 END) AS count2xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 300 AND 399 THEN 1 ELSE 0 END) AS count3xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 400 AND 499 THEN 1 ELSE 0 END) AS count4xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 500 AND 599 THEN 1 ELSE 0 END) AS count5xx, " +
            "COUNT(*) AS totalCount) " +
            "FROM DataUsageApi d " +
            "WHERE (:apiId IS NULL OR d.apiId = :apiId) " +
            "AND (:version IS NULL OR d.apiVersion = :version) " 
			+ "AND (:search IS NULL "
			+ "OR LOWER(d.apiResourceTemplate) LIKE LOWER(CONCAT('%', :search, '%'))  "
			+ "OR LOWER(d.apiMethod) LIKE LOWER(CONCAT('%', :search, '%'))  ) "+
            "GROUP BY d.apiResourceTemplate, d.apiMethod,d.apiId,d.apiName")
	Page<ErrorSummary> getAPIUsageByFilters(@Param("apiId") String apiId, @Param("version") String version,
			@Param("search") String search, Pageable pageable);

}
