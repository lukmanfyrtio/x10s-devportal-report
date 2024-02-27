package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummary {
	private int totalApis;
	private int requestCount;
	private int totalCustomers;
	private Page<ApiDetails> details;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ApiDetails {
		private String apiName;
		private String apiId;
		private String apiVersion;
		private String applicationName;
		private String applicationOwner;
		private BigInteger requestCount;
		private String applicationId;
		private String organization;
		private String startDate;
		private String endDate;
		private String tierId;
		private String subscriptionId;
		private String subsTypeId;

	}

}
