package com.integrixs.backend.logging;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database query logger and inspector for SQL tracking.
 * Logs queries, execution times, and detects slow queries.
 */
@Slf4j
@Component
public class DatabaseQueryLogger implements StatementInspector {

    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final Pattern TABLE_PATTERN = Pattern.compile("(?:FROM|JOIN|INTO|UPDATE|DELETE FROM)\\s + ([a - zA - Z_][a - zA - Z0-9_]*)", Pattern.CASE_INSENSITIVE);

    // Query statistics
    private final ConcurrentHashMap<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    private final ThreadLocal<QueryContext> queryContext = new ThreadLocal<>();

    /**
     * Inspect and potentially modify SQL before execution
     */
    @Override
    public String inspect(String sql) {
        // Start timing
        QueryContext context = new QueryContext();
        context.sql = sql;
        context.startTime = System.currentTimeMillis();
        context.correlationId = MDC.get("correlationId");
        context.userId = MDC.get("userId");
        context.operationId = MDC.get("operationId");

        queryContext.set(context);

        // Log query start
        String queryType = extractQueryType(sql);
        String tables = extractTables(sql);

        log.debug("#DB.QUERY.START# {}# {}# {}# {}# {}",
            queryType,
            tables,
            context.correlationId,
            context.userId,
            sql.length() > 200 ? sql.substring(0, 200) + "..." : sql
       );

        return sql;
    }

    /**
     * Log query completion(call this from a JPA event listener)
     */
    public void logQueryCompletion() {
        QueryContext context = queryContext.get();
        if(context == null) {
            return;
        }

        try {
            long duration = System.currentTimeMillis() - context.startTime;
            String queryType = extractQueryType(context.sql);
            String tables = extractTables(context.sql);

            // Update statistics
            String statsKey = queryType + ":" + tables;
            queryStats.computeIfAbsent(statsKey, k -> new QueryStats(queryType, tables))
                .recordExecution(duration);

            // Log based on duration
            if(duration >= SLOW_QUERY_THRESHOLD_MS) {
                log.warn("#DB.QUERY.SLOW# {}# {}# {}ms# {}# {}# {}",
                    queryType,
                    tables,
                    duration,
                    context.correlationId,
                    context.userId,
                    context.sql
               );
            } else {
                log.debug("#DB.QUERY.COMPLETE# {}# {}# {}ms# {}# {}",
                    queryType,
                    tables,
                    duration,
                    context.correlationId,
                    context.userId
               );
            }

            // Log query details for analysis
            if(log.isTraceEnabled()) {
                log.trace("SQL Query Details - Type: {}, Tables: {}, Duration: {}ms, SQL: {}",
                    queryType, tables, duration, context.sql);
            }

        } finally {
            queryContext.remove();
        }
    }

    /**
     * Log query error
     */
    public void logQueryError(Exception error) {
        QueryContext context = queryContext.get();
        if(context == null) {
            return;
        }

        try {
            long duration = System.currentTimeMillis() - context.startTime;
            String queryType = extractQueryType(context.sql);
            String tables = extractTables(context.sql);

            log.error("#DB.QUERY.ERROR# {}# {}# {}ms# {}# {}# {}# {}",
                queryType,
                tables,
                duration,
                error.getClass().getSimpleName(),
                error.getMessage(),
                context.correlationId,
                context.sql
           );

        } finally {
            queryContext.remove();
        }
    }

    /**
     * Extract query type from SQL
     */
    private String extractQueryType(String sql) {
        if(sql == null) return "UNKNOWN";

        String trimmed = sql.trim().toUpperCase();
        if(trimmed.startsWith("SELECT")) return "SELECT";
        if(trimmed.startsWith("INSERT")) return "INSERT";
        if(trimmed.startsWith("UPDATE")) return "UPDATE";
        if(trimmed.startsWith("DELETE")) return "DELETE";
        if(trimmed.startsWith("CREATE")) return "DDL";
        if(trimmed.startsWith("ALTER")) return "DDL";
        if(trimmed.startsWith("DROP")) return "DDL";
        if(trimmed.startsWith("CALL")) return "PROCEDURE";

        return "OTHER";
    }

    /**
     * Extract table names from SQL
     */
    private String extractTables(String sql) {
        if(sql == null) return "UNKNOWN";

        StringBuilder tables = new StringBuilder();
        Matcher matcher = TABLE_PATTERN.matcher(sql);

        while(matcher.find()) {
            if(tables.length() > 0) {
                tables.append(",");
            }
            tables.append(matcher.group(1));
        }

        return tables.length() > 0 ? tables.toString() : "UNKNOWN";
    }

    /**
     * Get query statistics
     */
    public ConcurrentHashMap<String, QueryStats> getQueryStats() {
        return new ConcurrentHashMap<>(queryStats);
    }

    /**
     * Reset query statistics
     */
    public void resetStats() {
        queryStats.clear();
        log.info("Database query statistics reset");
    }

    /**
     * Log current statistics
     */
    public void logStatistics() {
        log.info("#DB.STATS.START# Database Query Statistics Report");

        queryStats.forEach((key, stats) -> {
            log.info("#DB.STATS# {}# {}#count = {}#avgTime = {}ms#maxTime = {}ms#totalTime = {}ms",
                stats.getQueryType(),
                stats.getTables(),
                stats.getExecutionCount(),
                stats.getAverageTime(),
                stats.getMaxTime(),
                stats.getTotalTime()
           );
        });

        log.info("#DB.STATS.END# Total query types: {}", queryStats.size());
    }

    /**
     * Query context for tracking
     */
    private static class QueryContext {
        String sql;
        long startTime;
        String correlationId;
        String userId;
        String operationId;
    }

    /**
     * Query statistics
     */
    public static class QueryStats {
        private final String queryType;
        private final String tables;
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong maxTime = new AtomicLong(0);
        private volatile LocalDateTime lastExecuted;

        public QueryStats(String queryType, String tables) {
            this.queryType = queryType;
            this.tables = tables;
        }

        public void recordExecution(long duration) {
            executionCount.incrementAndGet();
            totalTime.addAndGet(duration);

            // Update max time
            long currentMax;
            do {
                currentMax = maxTime.get();
            } while(duration > currentMax && !maxTime.compareAndSet(currentMax, duration));

            lastExecuted = LocalDateTime.now();
        }

        public String getQueryType() { return queryType; }
        public String getTables() { return tables; }
        public long getExecutionCount() { return executionCount.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getMaxTime() { return maxTime.get(); }
        public LocalDateTime getLastExecuted() { return lastExecuted; }

        public double getAverageTime() {
            long count = executionCount.get();
            return count > 0 ? (double) totalTime.get() / count : 0;
        }
    }
}
