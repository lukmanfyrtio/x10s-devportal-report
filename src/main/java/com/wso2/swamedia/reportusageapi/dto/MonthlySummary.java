package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import org.springframework.data.domain.Page;

public class MonthlySummary {
	private int totalApis;
	private int requestCount;
	private Page<ApiDetails> details;

	public static class ApiDetails {
		private String apiName;
		private String apiId;
		private String apiVersion;
		private String applicationName;
		private String applicationOwner;
		private BigInteger requestCount;
		private String applicationId;

		
		
		public ApiDetails(String apiName, String apiId, String apiVersion, String applicationName,
				String applicationOwner, BigInteger requestCount,String applicationId) {
			super();
			this.apiName = apiName;
			this.apiId = apiId;
			this.apiVersion = apiVersion;
			this.applicationName = applicationName;
			this.applicationOwner = applicationOwner;
			this.requestCount = requestCount;
			this.applicationId = applicationId;
		}

		public String getApiId() {
			return apiId;
		}

		public void setApiId(String apiId) {
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

		public String getApplicationName() {
			return applicationName;
		}

		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}

		public String getApplicationOwner() {
			return applicationOwner;
		}

		public void setApplicationOwner(String applicationOwner) {
			this.applicationOwner = applicationOwner;
		}

		public BigInteger getRequestCount() {
			return requestCount;
		}

		public void setRequestCount(BigInteger requestCount) {
			this.requestCount = requestCount;
		}

		public String getApplicationId() {
			return applicationId;
		}

		public void setApplicationId(String applicationId) {
			this.applicationId = applicationId;
		}
		
		
	}
	
	

	public Page<ApiDetails> getDetails() {
		return details;
	}

	public void setDetails(Page<ApiDetails> details) {
		this.details = details;
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



}
