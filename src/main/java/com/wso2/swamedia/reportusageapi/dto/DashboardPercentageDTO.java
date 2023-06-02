package com.wso2.swamedia.reportusageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPercentageDTO {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String applicationId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String applicationName;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String apiId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String apiName;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String proxyResponseCode;
	private int rowCount;
	private double percentage;
}
