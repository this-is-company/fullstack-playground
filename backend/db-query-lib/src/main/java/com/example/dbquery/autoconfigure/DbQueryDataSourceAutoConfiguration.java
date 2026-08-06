package com.example.dbquery.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * consumer에 spring.datasource 설정이 없을 때,
 * 라이브러리 기본 접속정보로 DataSource를 만든다.
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
@ConditionalOnMissingBean(DataSource.class)
@EnableConfigurationProperties(DbQueryDataSourceProperties.class)
public class DbQueryDataSourceAutoConfiguration {

    @Bean
    public DataSource dataSource(DbQueryDataSourceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setPoolName("db-query-lib-pool");
        return dataSource;
    }
}
