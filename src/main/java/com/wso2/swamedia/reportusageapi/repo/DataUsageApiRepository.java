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
	
	//monthly summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request FROM DATA_USAGE_API "
			+ "WHERE APPLICATION_OWNER = :owner " + "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationName IS NULL OR APPLICATION_NAME = :applicationName)", nativeQuery = true)
	Map<String, Object> getTotalApisAndRequestsByOwnerAndFilters(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationName") String applicationName);

	
	//monthly summary table list
	@Query(value = "SELECT API_NAME,API_VERSION ,APPLICATION_OWNER ,API_ID, APPLICATION_NAME, COUNT(*) AS total_row_count ,APPLICATION_ID "
			+ "FROM DATA_USAGE_API " + "WHERE APPLICATION_OWNER = :owner "
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:applicationName IS NULL OR APPLICATION_NAME = :applicationName) "
			+ "AND (:search IS NULL OR API_NAME LIKE %:search% OR APPLICATION_NAME LIKE %:search%) "
			+ "GROUP BY APPLICATION_ID,API_NAME,API_VERSION ,APPLICATION_OWNER ,API_ID, APPLICATION_NAME " + "ORDER "
			+ " BY API_ID, APPLICATION_NAME ", countQuery = "SELECT COUNT(DISTINCT APPLICATION_ID) "
					+ "FROM DATA_USAGE_API " + "WHERE APPLICATION_OWNER = :owner "
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND (:apiId IS NULL OR API_ID = :apiId) "
					+ "AND (:applicationName IS NULL OR APPLICATION_NAME = :applicationName) "
					+ "AND (:search IS NULL OR API_NAME LIKE %:search% OR APPLICATION_NAME LIKE %:search%) ", nativeQuery = true)
	Page<Object[]> getMonthlyTotalRowByGroupByWithSearchAndPageable(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("applicationName") String applicationName, @Param("search") String search, Pageable pageable);

	
	
	@Query(value = "SELECT DATE_FORMAT(REQUEST_TIMESTAMP, '%Y-%b-%d %H:%i:%s') AS requestTimestamp, "
			+ "CONCAT(API_METHOD, ' ', API_RESOURCE_TEMPLATE) AS resource, "
			+ "PROXY_RESPONSE_CODE, API_ID, APPLICATION_ID " 
			+ "FROM DATA_USAGE_API " 
			+ "WHERE API_CREATOR = :owner "
			+ "AND APPLICATION_ID = :applicationId " 
			+ "AND API_ID = :apiId "
			+ "   AND (:searchFilter IS NULL OR (API_RESOURCE_TEMPLATE LIKE %:searchFilter%  "
			+ "    OR PROXY_RESPONSE_CODE LIKE %:searchFilter%  )) "
			+ "ORDER BY REQUEST_TIMESTAMP", countQuery = "SELECT COUNT(*) " + "FROM DATA_USAGE_API "
					+ "WHERE API_CREATOR = :owner " + "AND APPLICATION_ID = :applicationId " + "AND API_ID = :apiId "
					+ "    AND (:searchFilter IS NULL OR (API_RESOURCE_TEMPLATE LIKE %:searchFilter%  "
					+ "    OR PROXY_RESPONSE_CODE LIKE %:searchFilter% ))", nativeQuery = true)
	Page<Object[]> getDetailApplicationUsage(Pageable pageable, @Param("owner") String owner,
			@Param("applicationId") String applicationId, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter);

	
	//resource summary total APIs and total request count
	@Query(value = "SELECT COUNT(DISTINCT API_ID) AS total_apis, COUNT(*) AS total_request FROM DATA_USAGE_API "
			+ "WHERE APPLICATION_OWNER = :owner " + "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource)", nativeQuery = true)
	Map<String, Object> getResourceSumTotal(@Param("owner") String owner, @Param("year") Integer year,
			@Param("month") Integer month, @Param("apiId") String apiId, @Param("resource") String resource);
	
	
	//resource summary table list
	@Query(value = "SELECT API_NAME ,API_VERSION ,API_RESOURCE_TEMPLATE,API_METHOD ,COUNT(*) as request_count,API_ID "
			+ "FROM DATA_USAGE_API " 
			+ "WHERE APPLICATION_OWNER = :owner "
			+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
			+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
			+ "AND (:apiId IS NULL OR API_ID = :apiId) "
			+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource) "
			+ "AND (:search IS NULL OR API_NAME LIKE %:search% OR API_RESOURCE_TEMPLATE LIKE %:search%) "
			+ "GROUP BY API_NAME , API_VERSION , API_RESOURCE_TEMPLATE , API_METHOD,API_ID " 
			+ "ORDER "
			+ " BY request_count DESC", countQuery = "SELECT COUNT(DISTINCT API_RESOURCE_TEMPLATE,API_ID) "
					+ "FROM DATA_USAGE_API " 
					+ "WHERE APPLICATION_OWNER = :owner "
					+ "AND (:year IS NULL OR YEAR(REQUEST_TIMESTAMP) = :year) "
					+ "AND (:month IS NULL OR MONTH(REQUEST_TIMESTAMP) = :month) "
					+ "AND (:apiId IS NULL OR API_ID = :apiId) "
					+ "AND (:resource IS NULL OR API_RESOURCE_TEMPLATE = :resource) "
					+ "AND (:search IS NULL OR API_NAME LIKE %:search% OR API_RESOURCE_TEMPLATE LIKE %:search%) ", nativeQuery = true)
	Page<Object[]> getResourceSumList(@Param("owner") String owner, @Param("year") Integer year,
			@Param("month") Integer month, @Param("apiId") String apiId,
			@Param("resource") String resource, @Param("search") String search, Pageable pageable);
	
	@Query(value = "SELECT  APPLICATION_NAME ,API_NAME,COUNT(*) as request_count, "
			+ "COUNT(CASE WHEN PROXY_RESPONSE_CODE != 200 THEN 1 END) AS count_not_200,COUNT(CASE WHEN PROXY_RESPONSE_CODE = 200 THEN 1 END) AS count_200,"
			+ "API_ID, APPLICATION_ID " 
			+ "FROM DATA_USAGE_API " 
			+ "WHERE API_CREATOR = :owner "
			+ "AND API_RESOURCE_TEMPLATE = :resource " 
			+ "AND API_ID = :apiId "
			+ "   AND (:searchFilter IS NULL OR APPLICATION_NAME LIKE %:searchFilter%  "
			+ "    OR API_NAME LIKE %:searchFilter%  ) "
			+ "GROUP BY "
			+ "  APPLICATION_ID ,"
			+ "  APPLICATION_NAME ,"
			+ "  API_NAME, "
			+ "  API_ID "
			+ "ORDER BY request_count desc", countQuery = " SELECT COUNT(DISTINCT APPLICATION_ID)" + "FROM DATA_USAGE_API "
					+ "WHERE API_CREATOR = :owner "
					+ "AND API_RESOURCE_TEMPLATE = :resource " 
					+ "AND API_ID = :apiId "
					+ "AND (:searchFilter IS NULL OR APPLICATION_NAME LIKE %:searchFilter%  "
					+ "OR API_NAME LIKE %:searchFilter%  ) ", nativeQuery = true)
	Page<Object[]> getDetailLogResourceSum(Pageable pageable, @Param("owner") String owner,
			@Param("resource") String resource, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter);

}
