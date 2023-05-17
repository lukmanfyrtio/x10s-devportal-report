package com.wso2.swamedia.reportusageapi.dto;

public class MonthlySummaryDetails {
	private String requestTimestamp;
	private String resource;
	private Integer proxyResponseCode;
	private String apiId;
	private String applicationId;

	

	public MonthlySummaryDetails(String requestTimestamp, String resource, Integer proxyResponseCode, String apiId,
			String applicationId) {
		super();
		this.requestTimestamp = requestTimestamp;
		this.resource = resource;
		this.proxyResponseCode = proxyResponseCode;
		this.apiId = apiId;
		this.applicationId = applicationId;
	}

	public String getRequestTimestamp() {
		return requestTimestamp;
	}

	public void setRequestTimestamp(String requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Integer getProxyResponseCode() {
		return proxyResponseCode;
	}

	public void setProxyResponseCode(Integer proxyResponseCode) {
		this.proxyResponseCode = proxyResponseCode;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

}
