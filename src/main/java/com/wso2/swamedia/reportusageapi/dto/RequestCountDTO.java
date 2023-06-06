package com.wso2.swamedia.reportusageapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestCountDTO {
	private String apiResourceTemplate;
	private String apiMethod;
	private long requestCount;

}
