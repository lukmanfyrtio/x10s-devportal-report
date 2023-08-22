package com.wso2.swamedia.reportusageapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DBUtilsBilling {
	@Value("${spring.datasource.url}")
	private String databaseUrl;

	@Value("${spring.datasource.username}")
	private String databaseUsername;

	@Value("${spring.datasource.password}")
	private String databasePassword;

	@Value("${spring.billing-datasource.shcema}")
	private String schemaName;

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
	}

	public String getSchemaName() {
		return schemaName;
	}
}
