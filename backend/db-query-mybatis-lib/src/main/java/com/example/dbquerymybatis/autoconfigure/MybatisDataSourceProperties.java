package com.example.dbquerymybatis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db-query-mybatis.datasource")
public class MybatisDataSourceProperties {

    private String url = "jdbc:postgresql://localhost:5432/demodb";
    private String username = "demo";
    private String password = "demo123";
    private String driverClassName = "org.postgresql.Driver";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}
