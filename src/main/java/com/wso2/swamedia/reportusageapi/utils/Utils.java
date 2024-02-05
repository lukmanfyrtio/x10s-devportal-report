package com.wso2.swamedia.reportusageapi.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wso2.swamedia.reportusageapi.dto.SearchAPIResponse;
import com.wso2.swamedia.reportusageapi.dto.SubPoliciesResponse;
import com.wso2.swamedia.reportusageapi.dto.SubscriptionPolicies;
import com.wso2.swamedia.reportusageapi.service.QueryReport;

@Component
public class Utils {

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
	private String clientSecret;
	@Value("${wso2.base.url}")
	private String baseWso2Url;
	
	@Autowired
	private DBUtilsUser dbUtilsUser;
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

	RestTemplate restTemplate = restTemplateBuilder.requestFactory(() -> {
	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	    try {
			requestFactory.setHttpClient(HttpClientBuilder.create().setSSLContext(SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build()).build());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return requestFactory;
	}).build();
	public static boolean isAdmin(Map<String, Object> map) {
		// Cek apakah roles mengandung "Internal/admin"

		String roles = map.get("http://wso2.org/claims/role").toString();
		if (roles.contains("admin")) {
			return true;
		} else {
			return false;
		}
	}

	public SubscriptionPolicies getSubscriptionThrottlingPolicies(String apiId) {
		String url = baseWso2Url+"/api/am/devportal/v2/apis/" + apiId + "/subscription-policies";
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8);
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		

		try {
			ResponseEntity<SubscriptionPolicies> response = restTemplate.getForEntity(url, SubscriptionPolicies.class,
					headers);

			if (response.getStatusCodeValue() == 200) {
				return response.getBody();
			} else {
				System.out.println("Error: " + response.getStatusCodeValue());
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}

		return null;
	}

	public List<SubPoliciesResponse> getSubscriptionThrottlingPoliciesPublisher(String apiId) {
		String url = baseWso2Url+"/api/am/publisher/v3/apis/" + apiId + "/subscription-policies";
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8);
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		

		try {
			ResponseEntity<SubPoliciesResponse[]> response = restTemplate.exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), SubPoliciesResponse[].class);

			if (response.getStatusCodeValue() == 200) {
				return Arrays.asList(response.getBody());
			} else {
				System.out.println("Error: " + response.getStatusCodeValue());
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}

		return new ArrayList<>();
	}

	public static List<SubPoliciesResponse> getTierAttributesOfTypeTime(List<SubPoliciesResponse> dtoList) {
		List<SubPoliciesResponse> tierAttributesList = new ArrayList<>();

		for (SubPoliciesResponse dto : dtoList) {
			Map<String, String> tierAttributes = dto.getTierAttributes();
			if (tierAttributes != null && tierAttributes.containsKey("type_subscription")
					&& tierAttributes.get("type_subscription").equals("time")
					&& dto.getTierPlan().equals("COMMERCIAL")) {
				tierAttributesList.add(dto);
			}
		}

		return tierAttributesList;
	}
	
	public static List<SubPoliciesResponse> getTierAttributesOfTypeTimeAndQuota(List<SubPoliciesResponse> dtoList) {
		List<SubPoliciesResponse> tierAttributesList = new ArrayList<>();

		for (SubPoliciesResponse dto : dtoList) {
			Map<String, String> tierAttributes = dto.getTierAttributes();
			if (tierAttributes != null && tierAttributes.containsKey("type_subscription")
					&& tierAttributes.get("type_subscription").equals("time")
					&& dto.getTierPlan().equals("COMMERCIAL")) {
				tierAttributesList.add(dto);
			}else if (tierAttributes != null && tierAttributes.containsKey("type_subscription")
					&& tierAttributes.get("type_subscription").equals("quota")
					&& dto.getTierPlan().equals("COMMERCIAL")) {
				tierAttributesList.add(dto);
			}
		}

		return tierAttributesList;
	}
	
	public static List<SubPoliciesResponse> getTierAttributesOfTypeTimeAndFree(List<SubPoliciesResponse> dtoList) {
		List<SubPoliciesResponse> tierAttributesList = new ArrayList<>();

		for (SubPoliciesResponse dto : dtoList) {
			if (dto.getTierPlan().equals("FREE")) {
				tierAttributesList.add(dto);
			}
		}

		return tierAttributesList;
	}

	public SearchAPIResponse searchAPIs(String query, int limit, int offset) {
//        int limit = size;
//        int offset = (page - 1) * size;

		String url = baseWso2Url+"/api/am/devportal/v2/apis?query=" + query + "&limit=" + limit + "&offset="
				+ offset;
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8);
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		

		try {
			ResponseEntity<SearchAPIResponse> response = restTemplate.getForEntity(url, SearchAPIResponse.class,
					headers);

			if (response.getStatusCodeValue() == 200) {
				return response.getBody();
			} else {
				System.out.println("Error: " + response.getStatusCodeValue());
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}

		return null;
	}
	
	public int getTenantId() {
		try {
			String username = getUsername();
			String tenantName = getTenantFromUsername();
			Map<String, Object> params = new HashMap<>();
			params.put("tenantDomainName", tenantName);
			params.put("username", username);
			Map<String, Object> userData = executeSelectQuery(QueryReport.getUserData(dbUtilsUser.getSchemaName()), params);

			return Integer.valueOf(userData.get("UM_TENANT_ID").toString());
		} catch (Exception e) {
			return -1234;
		}
	}

	public String getUsernameByUserId(String userId) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("userId", userId);

			return executeSelectQueryString(QueryReport.getUsernameByUserId(dbUtilsUser.getSchemaName()), params);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getTenantFromUsername() {
		try {
			String input = SecurityContextHolder.getContext().getAuthentication().getName();
			if (input != null) {
				String[] parts = input.split("@");
				if (parts.length > 0) {
					return parts[1].trim();
				}
			}
			return input;
		} catch (Exception e) {
			return "carbon.super";
		}
	}

	public static String getUsername() {
		String input = SecurityContextHolder.getContext().getAuthentication().getName();
		if (input != null) {
			String[] parts = input.split("@");
			if (parts.length > 0) {
				return parts[0].trim();
			}
		}
		return input;
	}
	
	public int executeUpdate(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.update(sql, params);
		} catch (Exception e) {
			// Handle the exception as needed (log, throw, etc.)
			e.printStackTrace();
			return 0;
		}
	}

	public int executeSelectQueryInt(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
		} catch (Exception e) {
			// Handle the exception as needed (log, throw, etc.)
			e.printStackTrace();
			return 0;
		}
	}

	public String executeSelectQueryString(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
		} catch (Exception e) {
			// Handle the exception as needed (log, throw, etc.)
			e.printStackTrace();
			return "";
		}
	}
	
	public List<Map<String, Object>> executeSelectQueryList(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.queryForList(sql, params);
		} catch (Exception e) {
			// Handle the exception as needed (log, throw, etc.)
			e.printStackTrace();
			return new ArrayList<>(); // Return an empty list in case of an error
		}
	}
	
	public Map<String, Object> executeSelectQuery(String sql, Map<String, ?> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				Map<String, Object> result = new HashMap<>();

				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnName(i);
					Object columnValue = rs.getObject(i);
					result.put(columnName, columnValue);
				}

				return result;
			});
		} catch (Exception e) {
			// Handle the exception as needed (log, throw, etc.)
			e.printStackTrace();
			return new HashMap<>(); // Return an empty map in case of an error
		}
	}
}
