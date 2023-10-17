package com.wso2.swamedia.reportusageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorSummary {
	private String apiId;
	private String apiName;
	private String apiResourceTemplate;
	private String apiMethod;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long count1xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long count2xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long count3xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long count4xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long count5xx;
	private Long totalCount;
	
	
	
	


}
