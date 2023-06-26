package com.wso2.swamedia.reportusageapi.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class CustomOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
	private static final Logger logger =LoggerFactory.getLogger(CustomOpaqueTokenIntrospector.class);

	private final String introspectionUrl;
	private final String clientId;
	private final String clientSecret;

	@Value("${wso2.user-info.url}")
	private String piInfoUrl;

	public CustomOpaqueTokenIntrospector(String introspectionUrl, String clientId, String clientSecret) {
		this.introspectionUrl = introspectionUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}
	
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public OAuth2AuthenticatedPrincipal introspect(String token) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("token", token);

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

		try {

			ResponseEntity<Map> responseEntity = restTemplate.exchange(introspectionUrl, HttpMethod.POST, requestEntity,
					Map.class);
			Map<String, Object> response = responseEntity.getBody();

			if (response != null && (Boolean) response.get("active")) {
				String userId = (String) response.get("username");
				Collection<GrantedAuthority> authorities = extractAuthorities(response);

				Map<String, Object> claims = new HashMap<>(response);
				claims.remove("username");
				claims.remove("active");

				Instant expiration = response.containsKey("exp")
						? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue())
						: null;
				Instant iat = response.containsKey("iat")
						? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue())
						: null;
				claims.replace("exp", expiration);
				claims.replace("iat", iat);

				// Retrieve additional claims from the `/pi-info` API
				ResponseEntity<Map> piInfoResponseEntity = restTemplate.exchange(
						piInfoUrl + encodeUsernameAsPathParameter(userId), HttpMethod.GET, requestEntity, Map.class);

				Map<String, Object> piInfoResponse = piInfoResponseEntity.getBody();
				Map<String, Object> additionalClaims = (Map<String, Object>) piInfoResponse.get("basic");

				// Merge additional claims with existing claims
				if (additionalClaims != null) {
					claims.putAll(additionalClaims);
				}

				return new DefaultOAuth2AuthenticatedPrincipal(userId, claims, authorities);

			} else {
				throw new OAuth2IntrospectionException("Invalid token");
			}
		} catch (HttpClientErrorException.Unauthorized e) {
			// Handle 401 Unauthorized error
			logger.info("Unauthorized request: " + e.getMessage());
			throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Invalid token", null), e);
		} catch (OAuth2IntrospectionException e) {
			logger.info("Invalid token: " + e.getMessage());
			// Handle invalid token exception
			throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Invalid token", null), e);
		}
	}

	private Collection<GrantedAuthority> extractAuthorities(Map<String, Object> response) {
		if (response.containsKey("aut") && response.get("aut") instanceof String) {
			String authority = (String) response.get("aut");
			return Collections.singletonList(new SimpleGrantedAuthority(authority));
		}

		return null;
	}

	private String encodeUsernameAsPathParameter(String username) {
		int atIndex = username.indexOf('@');
		String usernamePart = (atIndex != -1) ? username.substring(0, atIndex) : username;
		byte[] encodedBytes = Base64.getEncoder().encode(usernamePart.getBytes(StandardCharsets.UTF_8));
		return new String(encodedBytes, StandardCharsets.UTF_8);
	}

}
