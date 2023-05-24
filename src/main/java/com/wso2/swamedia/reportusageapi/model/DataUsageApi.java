package com.wso2.swamedia.reportusageapi.model;

import java.time.LocalDateTime;

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

	private String apiName;
	private Integer proxyResponseCode;
	private String destination;
	private String apiCreatorTenantDomain;
	private String platform;
	private String apiMethod;
	private String apiVersion;
	private String gatewayType;
	private String apiCreator;
	private Boolean responseCacheHit;
	private Integer backendLatency;
	private String correlationId;
	private Integer requestMediationLatency;
	private String keyType;
	private String apiId;
	private String applicationName;
	private Integer targetResponseCode;
	private LocalDateTime requestTimestamp;
	private String applicationOwner;
	private String userAgent;
	private String eventType;
	private String apiResourceTemplate;
	private Integer responseLatency;
	private String regionId;
	private Integer responseMediationLatency;
	private String userIp;
	private String applicationId;
	private String apiType;

}
