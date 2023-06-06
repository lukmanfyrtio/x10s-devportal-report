package com.wso2.swamedia.reportusageapi.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataUsageApiResponse {
	private String apiId;
	private String apiName;
	private String apiVersion;
	private String context;
	private Long requestCount;
	private String applicationOwner;
	private String applicationId;
	private List<RequestCountDTO> details;
	public DataUsageApiResponse(String apiId, String apiName, String apiVersion, String context, Long requestCount,
			String applicationOwner, String applicationId) {
		super();
		this.apiId = apiId;
		this.apiName = apiName;
		this.apiVersion = apiVersion;
		this.context = context;
		this.requestCount = requestCount;
		this.applicationOwner = applicationOwner;
		this.applicationId = applicationId;
	}

	
	
	
}
