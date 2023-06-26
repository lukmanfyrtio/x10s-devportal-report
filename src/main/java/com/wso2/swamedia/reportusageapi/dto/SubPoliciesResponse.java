package com.wso2.swamedia.reportusageapi.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SubPoliciesResponse {
    private String name;
    @JsonProperty("displayName")
    private String displayName;
    private String description;
    private Map<String, String> policyContent;
    private Map<String, String> tierAttributes;
    private int requestsPerMin;
    private int requestCount;
    private int unitTime;
    private String timeUnit;
    private String tierPlan;
    private boolean stopOnQuotaReached;
    private Map<String, String> tierPermission;
    private Map<String, String> monetizationAttributes;
    private String quotaPolicyType;
    private int rateLimitCount;
    private String rateLimitTimeUnit;
    private Map<String, String> bandwidthDataUnit;
}
