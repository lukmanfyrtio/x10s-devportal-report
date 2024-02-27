package com.wso2.swamedia.reportusageapi.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSummaryDetails {
	private String applicationId;
	private String applicationName;
	private String apiId;
	private String apiName;
	private BigInteger requestCount;
	private BigInteger responseOK;
	private BigInteger responseNOK;
	private String applicationOwner;
	private String organization;
	private String startDate;
	private String endDate;
	private String tierId;
	private String subscriptionId;
	private String subsTypeId;


}
