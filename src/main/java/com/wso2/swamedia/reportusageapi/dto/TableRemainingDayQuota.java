package com.wso2.swamedia.reportusageapi.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableRemainingDayQuota {

	private String subscriptionId;
	private String applicationName;
	private String apiName;
	private String policyName;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer quota;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer apiUsage;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date startDate;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String typeSubscription;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer remaining;
	private String organization;
	private String applicationOwner;
	private String notes;
	private String subsStateId;
//    private Integer remainingDays;
}
