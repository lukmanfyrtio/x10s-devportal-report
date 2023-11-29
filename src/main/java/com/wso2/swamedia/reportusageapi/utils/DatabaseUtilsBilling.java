package com.wso2.swamedia.reportusageapi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUtilsBilling {
	@Value("${spring.billing-datasource.url}")
	private String databaseUrl;

	@Value("${spring.billing-datasource.username}")
	private String databaseUsername;

	@Value("${spring.billing-datasource.password}")
	private String databasePassword;

	@Value("${spring.billing-datasource.driver-class-name}")
	private String databaseDriverClassName;;

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
	}
	
	public String getSchemaName() {
		String[] urlParts = databaseUrl.split("/");
		String databaseName = urlParts[urlParts.length - 1];
		return databaseName;
	}
}
