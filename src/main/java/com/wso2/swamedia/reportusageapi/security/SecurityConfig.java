package com.wso2.swamedia.reportusageapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
	private String introspectionUrl;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
	private String clientSecret;

	@Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private static final String[] AUTH_WHITELIST = { "/swagger-resources", "/swagger-resources/**", "/configuration/ui",
			"/configuration/security", "/swagger-ui.html", "/webjars/**", "/v3/api-docs/**", "/api/public/**",
			"/api/public/authenticate", "**/actuator/*", "/swagger-ui/**","/actuator**" };

	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeRequests(requests -> requests
				.antMatchers(AUTH_WHITELIST).permitAll().anyRequest().authenticated())
				.exceptionHandling(handling -> handling.authenticationEntryPoint(customAuthenticationEntryPoint))
				.oauth2ResourceServer(server -> server.opaqueToken().introspector(introspector()));
		return http.build();
	}

	@Bean
	protected OpaqueTokenIntrospector introspector() {
		return new CustomOpaqueTokenIntrospector(introspectionUrl, clientId, clientSecret);
	}

}
