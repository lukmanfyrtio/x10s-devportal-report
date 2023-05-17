package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

public class ResourceSummaryDetails {
	private String applicationName;
	private BigInteger requestCount;
	private BigInteger responseOK;
	private BigInteger responseNOK;
	private String apiId;
	
	
	public ResourceSummaryDetails(String applicationName, BigInteger requestCount, BigInteger responseOK, BigInteger responseNOK,
			String apiId) {
		super();
		this.applicationName = applicationName;
		this.requestCount = requestCount;
		this.responseOK = responseOK;
		this.responseNOK = responseNOK;
		this.apiId = apiId;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public BigInteger getRequestCount() {
		return requestCount;
	}
	public void setRequestCount(BigInteger requestCount) {
		this.requestCount = requestCount;
	}
	public BigInteger getResponseOK() {
		return responseOK;
	}
	public void setResponseOK(BigInteger responseOK) {
		this.responseOK = responseOK;
	}
	public BigInteger getResponseNOK() {
		return responseNOK;
	}
	public void setResponseNOK(BigInteger responseNOK) {
		this.responseNOK = responseNOK;
	}
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	
	
}
