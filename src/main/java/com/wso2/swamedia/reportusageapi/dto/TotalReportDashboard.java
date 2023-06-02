package com.wso2.swamedia.reportusageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalReportDashboard {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalSubscriber;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalSubscriptionAPI;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalResponseFault;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalApi;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalApplication;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer totalUnpaid;

}
