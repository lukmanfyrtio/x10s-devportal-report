package com.wso2.swamedia.reportusageapi.repo.mysql;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wso2.swamedia.reportusageapi.dto.ApiProjection;
import com.wso2.swamedia.reportusageapi.model.AmApi;

@Profile("mysql")
@Repository
public interface AmApiRepositoryMySQL extends JpaRepository<AmApi, String> {
	

    @Query(value = "SELECT "
            + "a.API_UUID AS apiUuid, "
            + "a.API_ID AS apiId, "
            + "a.API_NAME AS apiName, "
            + "a.API_VERSION AS apiVersion, "
            + "a.CONTEXT AS context, "
            + "COUNT(d.API_ID) AS requestCount, "
            + "d.APPLICATION_OWNER AS applicationOwner, "
            + "d.APPLICATION_ID AS applicationId "
    		+ "FROM "
    		+ "    AM_API a "
    		+ "LEFT JOIN "
    		+ "    DATA_USAGE_API d ON a.API_UUID = d.API_ID "
    		+ "    LEFT JOIN shared_db.UM_USER uu ON "
    		+ "	d.APPLICATION_OWNER = uu.UM_USER_NAME "
    		+ "LEFT JOIN shared_db.UM_USER_ATTRIBUTE attr ON "
    		+ "	uu.UM_ID = attr.UM_USER_ID "
    		+ "	AND attr.UM_ATTR_NAME = 'organizationName' "
            + "WHERE "
            + "(:organization = 'PT Swamedia Informatika' OR attr.UM_ATTR_VALUE = :organization) "
            + "AND d.APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') "
            + "AND (:year IS NULL OR YEAR(d.REQUEST_TIMESTAMP) = :year) "
            + "AND (:month IS NULL OR MONTH(d.REQUEST_TIMESTAMP) = :month) "
            + "AND (:apiId IS NULL OR d.API_ID = :apiId) "
            + "AND (:keyType IS NULL OR d.KEY_TYPE = :keyType) "
            + "AND (:searchFilter IS NULL OR LOWER(a.CONTEXT) LIKE LOWER(CONCAT('%', :searchFilter, '%')) "
            + "OR LOWER(a.API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))) "
            + "GROUP BY "
            + "a.API_UUID, a.API_NAME, a.API_VERSION, a.CONTEXT, d.APPLICATION_OWNER, d.APPLICATION_ID "
            + "ORDER BY "
            + "requestCount DESC",
            countQuery = "SELECT COUNT(*) "
            		+ "FROM "
            		+ "    AM_API a "
            		+ "LEFT JOIN "
            		+ "    DATA_USAGE_API d ON a.API_UUID = d.API_ID "
            		+ "    LEFT JOIN shared_db.UM_USER uu ON "
            		+ "	d.APPLICATION_OWNER = uu.UM_USER_NAME "
            		+ "LEFT JOIN shared_db.UM_USER_ATTRIBUTE attr ON "
            		+ "	uu.UM_ID = attr.UM_USER_ID "
            		+ "	AND attr.UM_ATTR_NAME = 'organizationName' "
                    + "WHERE "
                    + "(:organization IS NULL OR attr.UM_ATTR_VALUE = :organization) "
                    + "AND d.APPLICATION_OWNER NOT IN ('anonymous', 'internal-key-app', 'UNKNOWN') "
                    + "AND (:year IS NULL OR YEAR(d.REQUEST_TIMESTAMP) = :year) "
                    + "AND (:month IS NULL OR MONTH(d.REQUEST_TIMESTAMP) = :month) "
                    + "AND (:apiId IS NULL OR d.API_ID = :apiId) "
                    + "AND (:keyType IS NULL OR d.KEY_TYPE = :keyType) "
                    + "AND (:searchFilter IS NULL OR LOWER(a.CONTEXT) LIKE LOWER(CONCAT('%', :searchFilter, '%')) "
                    + "OR LOWER(a.API_NAME) LIKE LOWER(CONCAT('%', :searchFilter, '%'))) ",
            nativeQuery = true)
    Page<ApiProjection> findByOwnerAndYearAndMonthAndApiIdAndSearchFilter(
            @Param("organization") String organization,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("apiId") String apiId,
            @Param("searchFilter") String searchFilter,
            @Param("keyType") String keyType,
            Pageable pageable);
	
}
