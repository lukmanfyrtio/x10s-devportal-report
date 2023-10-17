package com.wso2.swamedia.reportusageapi.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DATA_USAGE_API")
public class DataUsageApi {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "API_NAME")
    private String apiName;

    @Column(name = "PROXY_RESPONSE_CODE")
    private Integer proxyResponseCode;

    @Column(name = "DESTINATION")
    private String destination;

    @Column(name = "API_CREATOR_TENANT_DOMAIN")
    private String apiCreatorTenantDomain;

    @Column(name = "PLATFORM")
    private String platform;

    @Column(name = "API_METHOD")
    private String apiMethod;

    @Column(name = "API_VERSION")
    private String apiVersion;

    @Column(name = "GATEWAY_TYPE")
    private String gatewayType;

    @Column(name = "API_CREATOR")
    private String apiCreator;

    @Column(name = "RESPONSE_CACHE_HIT")
    private Boolean responseCacheHit;

    @Column(name = "BACKEND_LATENCY")
    private Integer backendLatency;

    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "REQUEST_MEDIATION_LATENCY")
    private Integer requestMediationLatency;

    @Column(name = "KEY_TYPE")
    private String keyType;

    @Column(name = "API_ID")
    private String apiId;

    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @Column(name = "TARGET_RESPONSE_CODE")
    private Integer targetResponseCode;

    @Column(name = "REQUEST_TIMESTAMP")
    private Timestamp requestTimestamp;

    @Column(name = "APPLICATION_OWNER")
    private String applicationOwner;

    @Column(name = "USER_AGENT")
    private String userAgent;

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Column(name = "API_RESOURCE_TEMPLATE")
    private String apiResourceTemplate;

    @Column(name = "RESPONSE_LATENCY")
    private Integer responseLatency;

    @Column(name = "REGION_ID")
    private String regionId;

    @Column(name = "RESPONSE_MEDIATION_LATENCY")
    private Integer responseMediationLatency;

    @Column(name = "USER_IP")
    private String userIp;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "API_TYPE")
    private String apiType;

    @Column(name = "CREATED_ON")
    private Timestamp createdOn;

    @Column(name = "SUBSCRIPTION_ID")
    private String subscriptionId;

}
