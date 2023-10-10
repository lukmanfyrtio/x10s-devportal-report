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

	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.RequestCountDTO(d.apiResourceTemplate, d.apiMethod, COUNT(d)) " +
	        "FROM DataUsageApi d " +
	        "WHERE d.apiId = :apiId " +
	        "AND d.keyType = :keyType "+
	        "AND applicationOwner NOT IN ('anonymous','internal-key-app','UNKNOWN') "+
	        "GROUP BY d.apiResourceTemplate, d.apiMethod")
	List<RequestCountDTO> countRequestByResource(@Param("apiId") String apiId, @Param("keyType") String keyType);
	
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.RequestCountDTO(d.apiResourceTemplate, d.apiMethod, COUNT(d)) " +
	        "FROM DataUsageApi d " +
	        "WHERE d.apiId = :apiId " +
	        "AND applicationOwner NOT IN ('anonymous','internal-key-app','UNKNOWN') "+
	        "AND d.keyType = :keyType "+
	        "GROUP BY d.apiResourceTemplate, d.apiMethod")
	Page<RequestCountDTO> countRequestByResource(@Param("apiId") String apiId,Pageable pageable,@Param("keyType") String keyType);
	
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.ErrorSummary(d.apiId,d.apiName,d.apiResourceTemplate, d.apiMethod, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 100 AND 199 THEN 1 ELSE 0 END) AS count1xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 200 AND 299 THEN 1 ELSE 0 END) AS count2xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 300 AND 399 THEN 1 ELSE 0 END) AS count3xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 400 AND 499 THEN 1 ELSE 0 END) AS count4xx, " +
            "SUM(CASE WHEN d.proxyResponseCode BETWEEN 500 AND 599 THEN 1 ELSE 0 END) AS count5xx, " +
            "COUNT(*) AS totalCount) " +
            "FROM DataUsageApi d " +
            "WHERE (:apiId IS NULL OR d.apiId = :apiId) " +
            "AND (:version IS NULL OR d.apiVersion = :version) " +
	        "AND d.keyType = :keyType "
			+ "AND (:search IS NULL "
			+ "OR LOWER(d.apiResourceTemplate) LIKE LOWER(CONCAT('%', :search, '%'))  "
			+ "OR LOWER(d.apiMethod) LIKE LOWER(CONCAT('%', :search, '%'))  ) "+
            "GROUP BY d.apiResourceTemplate, d.apiMethod,d.apiId,d.apiName")
	Page<ErrorSummary> getAPIUsageByFilters(@Param("apiId") String apiId, @Param("version") String version,
			@Param("search") String search, @Param("keyType") String keyType, Pageable pageable);

}
