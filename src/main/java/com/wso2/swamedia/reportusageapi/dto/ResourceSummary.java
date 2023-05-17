package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import org.springframework.data.domain.Page;

public class ResourceSummary {

	private int totalApis;
	private int requestCount;
	private Page<ApiDetails> details;

	public static class ApiDetails {
		private String apiName;
		private String apiVersion;
		private String resource;
		private String apiMethod;
		private BigInteger requestCount;
		private String apiId;
		
		
		
		public ApiDetails(String apiName, String apiVersion, String resource, String apiMethod, BigInteger requestCount,
				String apiId) {
			super();
			this.apiName = apiName;
			this.apiVersion = apiVersion;
			this.resource = resource;
			this.apiMethod = apiMethod;
			this.requestCount = requestCount;
			this.apiId = apiId;
		}
		public String getApiName() {
			return apiName;
		}
		public void setApiName(String apiName) {
			this.apiName = apiName;
		}
		public String getApiVersion() {
			return apiVersion;
		}
		public void setApiVersion(String apiVersion) {
			this.apiVersion = apiVersion;
		}
		public String getResource() {
			return resource;
		}
		public void setResource(String resource) {
			this.resource = resource;
		}
		public String getApiMethod() {
			return apiMethod;
		}
		public void setApiMethod(String apiMethod) {
			this.apiMethod = apiMethod;
		}
		public BigInteger getRequestCount() {
			return requestCount;
		}
		public void setRequestCount(BigInteger requestCount) {
			this.requestCount = requestCount;
		}
		public String getApiId() {
			return apiId;
		}
		public void setApiId(String apiId) {
			this.apiId = apiId;
		}
		
		
	}

	public int getTotalApis() {
		return totalApis;
	}

	public void setTotalApis(int totalApis) {
		this.totalApis = totalApis;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public Page<ApiDetails> getDetails() {
		return details;
	}

	public void setDetails(Page<ApiDetails> details) {
		this.details = details;
	}
	
	
}
