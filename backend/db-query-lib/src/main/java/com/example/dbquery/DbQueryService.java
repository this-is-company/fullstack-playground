package com.example.dbquery;

import com.example.dbquery.model.QueryResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reusable DB lookup service published to Nexus and consumed by other projects.
 */
public class DbQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public DbQueryService(JdbcTemplate jdbcTemplate) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate must not be null");
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public List<Map<String, Object>> findAll(String table) {
        validateIdentifier(table);
        return jdbcTemplate.queryForList("SELECT * FROM " + table + " ORDER BY 1");
    }

    public Optional<Map<String, Object>> findById(String table, String idColumn, Object id) {
        validateIdentifier(table);
        validateIdentifier(idColumn);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE " + idColumn + " = ?",
                id
        );
        return rows.stream().findFirst();
    }

    public List<Map<String, Object>> query(String sql, Object... args) {
        Assert.hasText(sql, "sql must not be blank");
        assertSelectOnly(sql);
        return jdbcTemplate.queryForList(sql, args);
    }

    public List<Map<String, Object>> queryNamed(String sql, Map<String, ?> params) {
        Assert.hasText(sql, "sql must not be blank");
        assertSelectOnly(sql);
        return namedJdbcTemplate.queryForList(sql, params);
    }

    public QueryResult queryAsResult(String sql, Object... args) {
        List<Map<String, Object>> rows = query(sql, args);
        return new QueryResult(rows.size(), rows);
    }

    public long count(String table) {
        validateIdentifier(table);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return count == null ? 0L : count;
    }

    private void assertSelectOnly(String sql) {
        String normalized = sql.trim().toLowerCase();
        if (!normalized.startsWith("select") && !normalized.startsWith("with")) {
            throw new IllegalArgumentException("Only SELECT/WITH queries are allowed");
        }
    }

    private void validateIdentifier(String identifier) {
        Assert.hasText(identifier, "identifier must not be blank");
        if (!identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }
}
