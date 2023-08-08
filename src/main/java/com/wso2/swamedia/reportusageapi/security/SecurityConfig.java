package com.wso2.swamedia.reportusageapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
	private String introspectionUrl;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
	private String clientSecret;

	@Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/**")
				.authenticated().anyRequest().permitAll().and().exceptionHandling()
				.authenticationEntryPoint(customAuthenticationEntryPoint).and().oauth2ResourceServer().opaqueToken()
				.introspector(introspector());

	}

	@Bean
	public OpaqueTokenIntrospector introspector() {
		return new CustomOpaqueTokenIntrospector(introspectionUrl, clientId, clientSecret);
	}

}
