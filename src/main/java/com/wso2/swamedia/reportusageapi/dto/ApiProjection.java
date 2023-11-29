package com.wso2.swamedia.reportusageapi.dto;

import java.util.List;

public interface ApiProjection {
	String getApiUuid();
	
	String getApiId();

	String getApiName();

	String getApiVersion();

	String getContext();

	Long getRequestCount();

	String getApplicationOwner();

	String getApplicationId();
	
	List<RequestCountDTO> getDetails();
}