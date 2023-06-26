package com.wso2.swamedia.reportusageapi.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SubscriptionPolicies {
	private String name;
	private String description;
	private String policyLevel;
	private Map<String, Object> attributes;
	private int requestCount;
	private String dataUnit;
	private int unitTime;
	private String timeUnit;
	private int rateLimitCount;
	private String rateLimitTimeUnit;
	private String quotaPolicyType;
	private String tierPlan;
	private boolean stopOnQuotaReach;
	private MonetizationAttributesDTO monetizationAttributes;
	private ThrottlingPolicyPermissionsDTO throttlingPolicyPermissions;

	// Getters and setters for all the fields
	@Data
	public static class MonetizationAttributesDTO {
		private String billingType;
		private String billingCycle;
		private String fixedPrice;
		private String pricePerRequest;
		private String currencyType;

		// Getters and setters for the MonetizationAttributesDTO fields
	}

	@Data
	public static class ThrottlingPolicyPermissionsDTO {
		private String type;
		private List<String> roles;

		// Getters and setters for the ThrottlingPolicyPermissionsDTO fields
	}
}
