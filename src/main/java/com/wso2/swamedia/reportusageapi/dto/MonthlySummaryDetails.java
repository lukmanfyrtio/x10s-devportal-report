package com.wso2.swamedia.reportusageapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryDetails {
	private String requestTimestamp;
	private String resource;
	private Integer proxyResponseCode;
	private String apiId;
	private String applicationId;
	private String apiName;
	private String applicationName;
	private String organization;

}
