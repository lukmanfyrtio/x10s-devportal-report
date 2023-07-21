package com.wso2.swamedia.reportusageapi;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wso2.swamedia.reportusageapi.dto.SearchAPIResponse;
import com.wso2.swamedia.reportusageapi.dto.SubPoliciesResponse;
import com.wso2.swamedia.reportusageapi.dto.SubscriptionPolicies;

@Component
public class Utils {

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
	private String clientSecret;
	@Value("${wso2.base.url}")
	private String baseWso2Url;
	
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
		if (roles.contains("Internal/admin")) {
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
}
