package com.wso2.swamedia.reportusageapi.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition( 
//		servers = {@Server(url = "https://apicentrum.store/v1/api/publisher")},
info = @Info(title = "APICentrum Devportal Report", description = "This is REST API for Revamp WSO2 Devportal", version = "v1.0"))
@SecurityScheme(
	    name = "bearerAuth",
	    type = SecuritySchemeType.HTTP,
	    bearerFormat = "JWT",
	    scheme = "bearer"
	)
public class OpenAPI3Configuration {

}
