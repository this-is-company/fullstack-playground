package com.example.dbquery.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DB 접속 기본값을 라이브러리에 내장한다.
 * consumer의 application.yml에서 db-query.datasource.* 로 덮어쓸 수 있다.
 *
 * <p>주의: 비밀번호가 jar/sources에 포함되므로 데모/학습용이다.
 */
@ConfigurationProperties(prefix = "db-query.datasource")
public class DbQueryDataSourceProperties {

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
