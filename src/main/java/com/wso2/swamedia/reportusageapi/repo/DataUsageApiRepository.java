package com.wso2.swamedia.reportusageapi.repo;

import java.util.List;

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
