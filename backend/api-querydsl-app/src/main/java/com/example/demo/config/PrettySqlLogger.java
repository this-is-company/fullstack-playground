package com.example.demo.config;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/** local 에서만 JDBC SQL. ? 자리에 값을 넣어 복사 실행 가능하게. */
@Component
@Profile("local")
public class PrettySqlLogger implements QueryExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(PrettySqlLogger.class);

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo query : queryInfoList) {
            String sql = prettySql(query.getQuery());
            log.info("[QUERYDSL→DB]\n{}", bindParams(sql, paramValues(query)));
        }
    }

    private static List<Object> paramValues(QueryInfo query) {
        if (query.getParametersList().isEmpty()) {
            return List.of();
        }
        return query.getParametersList().get(0).stream()
                .filter(op -> op.getArgs() != null && op.getArgs().length > 1)
                .map(op -> op.getArgs()[1])
                .toList();
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

    private static String prettySql(String sql) {
        return sql.replaceAll("(?i)\\s+from\\s+", "\nFROM ")
                .replaceAll("(?i)\\s+where\\s+", "\nWHERE ")
                .replaceAll("(?i)\\s+and\\s+", "\n  AND ")
                .replaceAll("(?i)\\s+values\\s+", "\nVALUES ")
                .replaceAll("(?i)\\s+set\\s+", "\nSET ")
                .replaceAll("(?i)\\s+order by\\s+", "\nORDER BY ")
                .strip();
    }
}
