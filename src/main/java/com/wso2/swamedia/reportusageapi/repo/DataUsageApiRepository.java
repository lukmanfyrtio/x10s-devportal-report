package com.wso2.swamedia.reportusageapi.repo;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wso2.swamedia.reportusageapi.model.DataUsageApi;

@Repository
public interface DataUsageApiRepository extends JpaRepository<DataUsageApi, String> {

	// monthly summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request "
			+ "FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId)", nativeQuery = true)
	Map<String, Object> getTotalApisAndRequestsByOwnerAndFilters(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationId") String applicationId);

	
	// monthly summary table list
	@Query(value = "SELECT API_NAME,API_VERSION ,APPLICATION_OWNER ,API_ID, APPLICATION_NAME, "
			+ "COUNT(*) AS total_row_count ,APPLICATION_ID "
			+ "FROM DATA_USAGE_API " 
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId) "
			+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "GROUP BY APPLICATION_ID,API_NAME,API_VERSION "
			+ ",APPLICATION_OWNER ,API_ID, APPLICATION_NAME " 
			+ "ORDER "
			+ " BY API_ID, APPLICATION_NAME ", countQuery = "SELECT COUNT(DISTINCT APPLICATION_ID) "
					+ "FROM DATA_USAGE_API " 
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND (:apiId IS NULL OR API_ID = :apiId) "
					+ "AND (:applicationId IS NULL OR APPLICATION_ID = :applicationId) "
					+ "AND (:search IS NULL OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :search, '%'))) ", nativeQuery = true)
	Page<Object[]> getMonthlyTotalRowByGroupByWithSearchAndPageable(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationId") String applicationId, @Param("search") String search, Pageable pageable);

	@Query(value = "SELECT DATE_FORMAT(REQUEST_TIMESTAMP, '%Y-%b-%d %H:%i:%s') AS requestTimestamp, "
			+ "CONCAT(API_METHOD, ' ', API_RESOURCE_TEMPLATE) AS resource, "
			+ "PROXY_RESPONSE_CODE, API_ID, APPLICATION_ID ,API_NAME,APPLICATION_NAME " 
			+ "FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" 
			+ "AND APPLICATION_ID = :applicationId "
			+ "AND API_ID = :apiId " 
			+ "   AND (:searchFilter IS NULL "
			+ "OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
			+ "    OR LOWER(PROXY_RESPONSE_CODE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  )) "
			+ "ORDER BY REQUEST_TIMESTAMP", 
			countQuery = "SELECT COUNT(*) " 
			+ "FROM DATA_USAGE_API "
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" 
					+ "AND APPLICATION_ID = :applicationId "
					+ "AND API_ID = :apiId "
					+ "    AND (:searchFilter IS NULL "
					+ "OR (LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
					+ "    OR LOWER(PROXY_RESPONSE_CODE) LIKE LOWER(CONCAT('%', :searchFilter, '%')) ))", 
					nativeQuery = true)
	Page<Object[]> getMonthlyDetailLog(Pageable pageable, @Param("owner") String owner,
			@Param("applicationId") String applicationId, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter);

	// resource summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) " 
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
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
					+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource) "
					+ "AND (:search IS NULL OR LOWER(API_NAME LIKE) LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(API_RESOURCE_TEMPLATE) LIKE LOWER(CONCAT('%', :search, '%'))) ", 
					nativeQuery = true)
	Page<Object[]> getResourceSumList(@Param("owner") String owner, @Param("year") Integer year,
			@Param("month") Integer month, @Param("apiId") String apiId, @Param("resource") String resource,
			@Param("search") String search, Pageable pageable);

	@Query(value = "SELECT  APPLICATION_NAME ,API_NAME,COUNT(*) as request_count, "
			+ "COUNT(CASE WHEN PROXY_RESPONSE_CODE != 200 THEN 1 END) AS count_not_200,COUNT(CASE WHEN PROXY_RESPONSE_CODE = 200 THEN 1 END) AS count_200,"
			+ "API_ID, APPLICATION_ID ,REQUEST_TIMESTAMP,APPLICATION_OWNER " 
			+ " FROM DATA_USAGE_API "
			+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )" + "AND API_RESOURCE_TEMPLATE = :resource "
			+ "AND API_ID = :apiId " 
			+ "   AND (:searchFilter IS NULL OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
			+ "    OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  ) " 
			+ "GROUP BY " 
			+ "  APPLICATION_ID ," 
			+ "  APPLICATION_NAME ,"
			+ "  API_NAME, " 
			+ "  API_ID ,"
			+ "REQUEST_TIMESTAMP,"
			+ "APPLICATION_OWNER "
			+ "ORDER BY request_count desc", 
			countQuery = " SELECT COUNT(DISTINCT APPLICATION_ID)"
					+ "FROM DATA_USAGE_API " 
					+ "WHERE (:owner IS NULL OR APPLICATION_OWNER = :owner )"
					+ "AND API_RESOURCE_TEMPLATE = :resource " + "AND API_ID = :apiId "
					+ "AND (:searchFilter IS NULL "
					+ "OR LOWER(APPLICATION_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  "
					+ "OR LOWER(API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))  ) ", 
					nativeQuery = true)
	Page<Object[]> getDetailLogResourceSum(Pageable pageable, @Param("owner") String owner,
			@Param("resource") String resource, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter);

}
