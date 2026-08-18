package com.example.demo.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** local 프로필에서만 SQL 로그. */
@Component
@Profile("local")
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        }),
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class
        })
})
public class MybatisSqlLoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(MybatisSqlLoggingInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object param = invocation.getArgs()[1];
        BoundSql boundSql = ms.getBoundSql(param);
        String sql = prettySql(boundSql.getSql());
        String runnable = bindParams(sql, paramsList(ms, boundSql, param));
        log.info("[MYBATIS→DB] {}\n{}", ms.getId(), runnable);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    private static String prettySql(String sql) {
        String[] lines = sql.strip().split("\\R");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(line.strip());
            }
        }
        return sb.toString()
                .replaceAll("(?i)\\s+FROM\\s+", "\nFROM ")
                .replaceAll("(?i)\\s+WHERE\\s+", "\nWHERE ")
                .replaceAll("(?i)\\s+AND\\s+", "\n  AND ")
                .replaceAll("(?i)\\s+SET\\s+", "\nSET ")
                .replaceAll("(?i)\\s+VALUES\\s+", "\nVALUES ")
                .replaceAll("(?i)\\s+ORDER BY\\s+", "\nORDER BY ");
    }

    private static List<Object> paramsList(MappedStatement ms, BoundSql boundSql, Object param) {
        List<Object> values = new ArrayList<>();
        TypeHandlerRegistry registry = ms.getConfiguration().getTypeHandlerRegistry();
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String property = mapping.getProperty();
            Object value;
            if (boundSql.hasAdditionalParameter(property)) {
                value = boundSql.getAdditionalParameter(property);
            } else if (param == null) {
                value = null;
            } else if (registry.hasTypeHandler(param.getClass())) {
                value = param;
            } else {
                MetaObject meta = ms.getConfiguration().newMetaObject(param);
                value = meta.hasGetter(property) ? meta.getValue(property) : null;
            }
            values.add(value);
        }
        return values;
    }

    private static String bindParams(String sql, List<Object> values) {
        StringBuilder sb = new StringBuilder();
        int from = 0;
        int idx = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?' && idx < values.size()) {
                sb.append(sql, from, i);
                sb.append(formatValue(values.get(idx++)));
                from = i + 1;
            }
        }
        sb.append(sql.substring(from));
        return sb.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        String text = value instanceof Enum<?> e ? e.name() : String.valueOf(value);
        return "'" + text.replace("'", "''") + "'";
    }
}
