package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSummary {

	private int totalApis;
	private int requestCount;
	private Page<ApiDetails> details;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ApiDetails {
		private String apiName;
		private String apiVersion;
		private String resource;
		private String apiMethod;
		private BigInteger requestCount;
		private String apiId;
		private String applicationId;
		private String applicationName;

	}

}
