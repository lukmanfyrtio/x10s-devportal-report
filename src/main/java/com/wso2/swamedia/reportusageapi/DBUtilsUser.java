package com.wso2.swamedia.reportusageapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DBUtilsUser {
	@Value("${spring.second-datasource.url}")
	private String databaseUrl;

	@Value("${spring.second-datasource.username}")
	private String databaseUsername;

	@Value("${spring.second-datasource.password}")
	private String databasePassword;

	@Value("${spring.second-datasource.driver-class-name}")
	private String databaseDriverClassName;;

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
	}
}
