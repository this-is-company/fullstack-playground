package com.example.dbquery.autoconfigure;

import com.example.dbquery.DbQueryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(JdbcTemplate.class)
public class DbQueryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DbQueryService dbQueryService(JdbcTemplate jdbcTemplate) {
        return new DbQueryService(jdbcTemplate);
    }
}
