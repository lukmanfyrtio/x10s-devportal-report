package com.wso2.swamedia.reportusageapi;

//@EnableWebSecurity
public class SecurityConfig {

//	protected void configure(HttpSecurity http) throws Exception {
//
//		http.authorizeRequests()
//
//				// allow anonymous access to the root page
//				.antMatchers("/").permitAll()
//
//				// all other requests
//				.anyRequest().authenticated()
//
//				// Replace with logoutSuccessHandler(oidcLogoutSuccessHandler()) to support OIDC
//				// RP-initiated logout
//				.and().logout().logoutSuccessHandler(oidcLogoutSuccessHandler())
//
//				// enable OAuth2/OIDC
//				.and().oauth2Login();
//
//	}
//
//	// Inject the ClientRegistrationRepository which stores client registration
//	// information
//	@Autowired
//	private ClientRegistrationRepository clientRegistrationRepository;
//
//	/**
//	 * Create a OidcClientInitiatedLogoutSuccessHandler
//	 *
//	 * @return OidcClientInitiatedLogoutSuccessHandler
//	 */
//	private LogoutSuccessHandler oidcLogoutSuccessHandler() {
//
//		OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(
//				this.clientRegistrationRepository);
//
//		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8081/login"); // Need to give the post-rediret-uri
//																					// here
//
//		return oidcLogoutSuccessHandler;
//	}

}