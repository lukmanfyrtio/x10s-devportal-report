package com.wso2.swamedia.reportusageapi.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchAPIResponse {
	private int count;
	private List<ApiDTO> list;
	private PaginationDTO pagination;

	// Getters and setters for all the fields
	@Data
	public static class ApiDTO {
		private String id;
		private String name;
		private String description;
		private String context;
		private String version;
		private String type;
		private String provider;
		private String lifeCycleStatus;
		private String thumbnailUri;
		private double avgRating;
		private List<String> throttlingPolicies;
		private AdvertiseInfoDTO advertiseInfo;
		private BusinessInformationDTO businessInformation;
		private boolean isSubscriptionAvailable;
		private String monetizationLabel;

		// Getters and setters for the ApiDTO fields
		@Data
		public static class AdvertiseInfoDTO {
			private String advertised;
			private String originalDevPortalUrl;
			private String apiOwner;

			// Getters and setters for the AdvertiseInfoDTO fields
		}

		@Data
		public static class BusinessInformationDTO {
			private String businessOwner;
			private String businessOwnerEmail;
			private String technicalOwner;
			private String technicalOwnerEmail;

			// Getters and setters for the BusinessInformationDTO fields
		}
	}

	@Data
	public static class PaginationDTO {
		private int offset;
		private int limit;
		private int total;
		private String next;
		private String previous;

		// Getters and setters for the PaginationDTO fields
	}
}
