package com.nikhil.datasource.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.core.env.Environment;

import com.nikhil.datasource.dto.TenantSource;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;

public class DataSourceLoader {

	private final Map<Object, Object> accountVSDataSourceMap = new HashMap<>();

	private final Environment environment;

	public DataSourceLoader(Environment environment) {
		this.environment = environment;
	}

	@PostConstruct
	private void loadAllDataSource() {
		List<TenantSource> tenantSources = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(environment.getProperty("db.datasource.url"),
				environment.getProperty("db.datasource.username"), environment.getProperty("db.datasource.password"));
				PreparedStatement prepareStatement = connection
						.prepareStatement("SELECT * FROM tenent_source WHERE deleted=0");
				ResultSet result = prepareStatement.executeQuery();) {
			while (result.next()) {
				TenantSource tenantSource = new TenantSource(result.getString("name"), result.getString("type"),
						result.getString("host"), result.getString("driver"), result.getString("params"),
						result.getString("port"));
				tenantSources.add(tenantSource);
			}
			tenantSources.forEach(tenantSource -> {
				accountVSDataSourceMap.put(tenantSource.name().toUpperCase(), constructDataSource(tenantSource));
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Map<Object, Object> getAllDataSource() {
		if (accountVSDataSourceMap.isEmpty()) {
			throw new RuntimeException("Data Source is empty");
		}
		return accountVSDataSourceMap;
	}

	private DataSource constructDataSource(TenantSource tenentSource) {
		HikariDataSource dataSource = new HikariDataSource();

		dataSource.setMaximumPoolSize(5);
		dataSource.setDriverClassName(tenentSource.driver());
		dataSource.setJdbcUrl("jdbc:%s://%s:%s/%s?%s".formatted(tenentSource.type(), tenentSource.host(),
				tenentSource.port(), tenentSource.name(), tenentSource.params()));
		dataSource.setUsername(environment.getProperty("db.datasource.username"));
		dataSource.setPassword(environment.getProperty("db.datasource.password"));
		dataSource.setPoolName(tenentSource.name().toUpperCase());

		return dataSource;
	}

}
