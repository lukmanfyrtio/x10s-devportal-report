package com.wso2.swamedia.reportusageapi.repo.mysql;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wso2.swamedia.reportusageapi.dto.DataUsageApiResponse;
import com.wso2.swamedia.reportusageapi.model.AmApi;

@Profile("mysql")
@Repository
public interface AmApiRepositoryMySQL extends JpaRepository<AmApi, String> {
	
	@Query("SELECT new com.wso2.swamedia.reportusageapi.dto.DataUsageApiResponse(a.apiUuid, "
			+ "a.apiName, a.apiVersion, a.context, COUNT(d.apiId) AS requestCount,d.applicationOwner,d.applicationId) "
			+ "FROM AmApi a " 
			+ "LEFT JOIN DataUsageApi d ON a.apiUuid = d.apiId "
			+ "WHERE (:owner IS NULL OR d.applicationOwner = :owner) "
			+"AND d.applicationOwner NOT IN ('anonymous','internal-key-app','UNKNOWN') "
			+ "AND (:year IS NULL OR YEAR(d.requestTimestamp) = :year) "
			+ "AND (:month IS NULL OR MONTH(d.requestTimestamp) = :month) "
			+ "AND (:apiId IS NULL OR d.apiId = :apiId) "
			+ "AND  d.keyType = :keyType "
			+ "AND (:searchFilter IS NULL OR LOWER(a.context) LIKE LOWER(CONCAT('%', :searchFilter, '%')) "
			+ "OR LOWER(a.apiName) LIKE LOWER(CONCAT('%', :searchFilter, '%'))) "
			+ "GROUP BY a.apiId, a.apiName, a.apiVersion, a.context ,d.applicationOwner,d.applicationId " + "ORDER BY requestCount DESC")
	Page<DataUsageApiResponse> findByOwnerAndYearAndMonthAndApiIdAndSearchFilter(@Param("owner") String owner,
			@Param("year") Integer year, @Param("month") Integer month, @Param("apiId") String apiId,
			@Param("searchFilter") String searchFilter,@Param("keyType") String keyType ,Pageable pageable);
	
}
