package com.wso2.swamedia.reportusageapi.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public Integer getProxyResponseCode() {
		return proxyResponseCode;
	}

	public void setProxyResponseCode(Integer proxyResponseCode) {
		this.proxyResponseCode = proxyResponseCode;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getApiCreatorTenantDomain() {
		return apiCreatorTenantDomain;
	}

	public void setApiCreatorTenantDomain(String apiCreatorTenantDomain) {
		this.apiCreatorTenantDomain = apiCreatorTenantDomain;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getApiMethod() {
		return apiMethod;
	}

	public void setApiMethod(String apiMethod) {
		this.apiMethod = apiMethod;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(String gatewayType) {
		this.gatewayType = gatewayType;
	}

	public String getApiCreator() {
		return apiCreator;
	}

	public void setApiCreator(String apiCreator) {
		this.apiCreator = apiCreator;
	}

	public Boolean getResponseCacheHit() {
		return responseCacheHit;
	}

	public void setResponseCacheHit(Boolean responseCacheHit) {
		this.responseCacheHit = responseCacheHit;
	}

	public Integer getBackendLatency() {
		return backendLatency;
	}

	public void setBackendLatency(Integer backendLatency) {
		this.backendLatency = backendLatency;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public Integer getRequestMediationLatency() {
		return requestMediationLatency;
	}

	public void setRequestMediationLatency(Integer requestMediationLatency) {
		this.requestMediationLatency = requestMediationLatency;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Integer getTargetResponseCode() {
		return targetResponseCode;
	}

	public void setTargetResponseCode(Integer targetResponseCode) {
		this.targetResponseCode = targetResponseCode;
	}

	public LocalDateTime getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(LocalDateTime requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public String getApplicationOwner() {
		return applicationOwner;
	}

	public void setApplicationOwner(String applicationOwner) {
		this.applicationOwner = applicationOwner;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getApiResourceTemplate() {
		return apiResourceTemplate;
	}

	public void setApiResourceTemplate(String apiResourceTemplate) {
		this.apiResourceTemplate = apiResourceTemplate;
	}

	public Integer getResponseLatency() {
		return responseLatency;
	}

	public void setResponseLatency(Integer responseLatency) {
		this.responseLatency = responseLatency;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public Integer getResponseMediationLatency() {
		return responseMediationLatency;
	}

	public void setResponseMediationLatency(Integer responseMediationLatency) {
		this.responseMediationLatency = responseMediationLatency;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

}
