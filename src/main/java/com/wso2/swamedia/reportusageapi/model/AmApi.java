package com.wso2.swamedia.reportusageapi.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "AM_API")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmApi {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "API_ID")
	private int apiId;

	@Column(name = "API_UUID")
	private String apiUuid;

	@Column(name = "API_PROVIDER")
	private String apiProvider;

	@Column(name = "API_NAME")
	private String apiName;

	@Column(name = "API_VERSION")
	private String apiVersion;

	@Column(name = "CONTEXT")
	private String context;

	@Column(name = "CONTEXT_TEMPLATE")
	private String contextTemplate;

	@Column(name = "API_TIER")
	private String apiTier;

	@Column(name = "API_TYPE")
	private String apiType;

	@Column(name = "ORGANIZATION")
	private String organization;

	@Column(name = "GATEWAY_VENDOR")
	private String gatewayVendor;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "CREATED_TIME")
	private Timestamp createdTime;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "UPDATED_TIME")
	private Timestamp updatedTime;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "LOG_LEVEL")
	private String logLevel;

	@Column(name = "REVISIONS_CREATED")
	private int revisionsCreated;

	@Column(name = "VERSION_COMPARABLE")
	private String versionComparable;
}
