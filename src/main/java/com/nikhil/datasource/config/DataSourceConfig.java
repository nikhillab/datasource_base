package com.nikhil.datasource.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.nikhil.datasource.routing.TenantAwareRoutingSource;
import com.nikhil.datasource.routing.ThreadLocalStorage;

@Configuration
public class DataSourceConfig {

	@Autowired
	private Environment environment;

	@Bean
	public DataSource dataSource() {
		AbstractRoutingDataSource routingSource = new TenantAwareRoutingSource();
		routingSource.setTargetDataSources(dataSourceLoader().getAllDataSource());
		routingSource.setDefaultTargetDataSource(
				dataSourceLoader().getAllDataSource().get(environment.getProperty("db.default")));
		ThreadLocalStorage.setTenantName(environment.getProperty("db.default"));
		return routingSource;

	}

	@Bean
	public DataSourceLoader dataSourceLoader() {
		return new DataSourceLoader(environment);
	}
}
