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

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double percent1xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double percent2xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double percent3xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double percent4xx;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double percent5xx;
	public ErrorSummary(String apiId, String apiName, String apiResourceTemplate, String apiMethod, Long count1xx,
			Long count2xx, Long count3xx, Long count4xx, Long count5xx, Long totalCount) {
		super();
		this.apiId = apiId;
		this.apiName = apiName;
		this.apiResourceTemplate = apiResourceTemplate;
		this.apiMethod = apiMethod;
		this.count1xx = count1xx;
		this.count2xx = count2xx;
		this.count3xx = count3xx;
		this.count4xx = count4xx;
		this.count5xx = count5xx;
		this.totalCount = totalCount;
	}



}
