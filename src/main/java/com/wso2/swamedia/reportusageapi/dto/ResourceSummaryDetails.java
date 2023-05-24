package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSummaryDetails {
	private String applicationName;
	private BigInteger requestCount;
	private BigInteger responseOK;
	private BigInteger responseNOK;
	private String apiId;

}
