package com.wso2.swamedia.reportusageapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyAccountController {

	@GetMapping("/v1/me-info")
	public Object getSampleData(Authentication authentication) {
		DefaultOAuth2AuthenticatedPrincipal principal = (DefaultOAuth2AuthenticatedPrincipal) authentication
				.getPrincipal();
		return principal;
	}
}
