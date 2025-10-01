package com.integrixs.backend.performance;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic connection pool tuner that monitors and adjusts pool sizes based on usage patterns.
 */
public class ConnectionPoolTuner {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolTuner.class);


    private final DataSource dataSource;
    private final PoolingHttpClientConnectionManager httpConnectionManager;
    private final MeterRegistry meterRegistry;

    // Tuning thresholds
    private static final double HIGH_UTILIZATION_THRESHOLD = 0.8;
    private static final double LOW_UTILIZATION_THRESHOLD = 0.3;
    private static final double CRITICAL_UTILIZATION_THRESHOLD = 0.95;

    // Adjustment factors
    private static final double INCREASE_FACTOR = 1.2;
    private static final double DECREASE_FACTOR = 0.8;

    // Limits
    private static final int MAX_DB_POOL_SIZE = 50;
    private static final int MIN_DB_POOL_SIZE = 5;
    private static final int MAX_HTTP_CONNECTIONS = 500;
    private static final int MIN_HTTP_CONNECTIONS = 20;

    // Constructor
    public ConnectionPoolTuner(DataSource dataSource,
                             PoolingHttpClientConnectionManager httpConnectionManager,
                             MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.httpConnectionManager = httpConnectionManager;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Periodically tune connection pools based on utilization.
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void tuneConnectionPools() {
        try {
            tuneDatabasePool();
            tuneHttpPool();
        } catch(Exception e) {
            log.error("Error tuning connection pools", e);
        }
    }

    private void tuneDatabasePool() {
        if(!(dataSource instanceof HikariDataSource)) {
            return;
        }

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

        if(poolMXBean == null) {
            return;
        }

        int totalConnections = poolMXBean.getTotalConnections();
        int activeConnections = poolMXBean.getActiveConnections();
        int idleConnections = poolMXBean.getIdleConnections();
        int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();

        double utilization = totalConnections > 0 ?
            (double) activeConnections / totalConnections : 0;

        log.debug("DB Pool - Total: {}, Active: {}, Idle: {}, Waiting: {}, Utilization: {:.2f}%",
                totalConnections, activeConnections, idleConnections,
                threadsAwaitingConnection, utilization * 100);

        // Record metrics
        meterRegistry.gauge("db.pool.utilization", utilization);
        meterRegistry.gauge("db.pool.threads.waiting", threadsAwaitingConnection);

        // Critical condition - threads waiting and high utilization
        if(threadsAwaitingConnection > 0 && utilization > CRITICAL_UTILIZATION_THRESHOLD) {
            int currentMax = hikariDataSource.getMaximumPoolSize();
            int newMax = Math.min((int) (currentMax * INCREASE_FACTOR), MAX_DB_POOL_SIZE);

            if(newMax > currentMax) {
                hikariDataSource.setMaximumPoolSize(newMax);
                log.warn("Increased DB pool size from {} to {} due to high demand", currentMax, newMax);

                // Also increase minimum idle to prevent thrashing
                int newMinIdle = Math.max(hikariDataSource.getMinimumIdle() + 1, newMax / 4);
                hikariDataSource.setMinimumIdle(newMinIdle);
            }
        }
        // High utilization - proactive increase
        else if(utilization > HIGH_UTILIZATION_THRESHOLD) {
            int currentMax = hikariDataSource.getMaximumPoolSize();
            int newMax = Math.min((int) (currentMax * 1.1), MAX_DB_POOL_SIZE);

            if(newMax > currentMax) {
                hikariDataSource.setMaximumPoolSize(newMax);
                log.info("Proactively increased DB pool size from {} to {}", currentMax, newMax);
            }
        }
        // Low utilization - decrease to save resources
        else if(utilization < LOW_UTILIZATION_THRESHOLD && totalConnections > MIN_DB_POOL_SIZE) {
            int currentMax = hikariDataSource.getMaximumPoolSize();
            int newMax = Math.max((int) (currentMax * DECREASE_FACTOR), MIN_DB_POOL_SIZE);

            if(newMax < currentMax) {
                hikariDataSource.setMaximumPoolSize(newMax);
                log.info("Decreased DB pool size from {} to {} due to low utilization", currentMax, newMax);

                // Adjust minimum idle proportionally
                int newMinIdle = Math.max(1, newMax / 4);
                hikariDataSource.setMinimumIdle(newMinIdle);
            }
        }
    }

    private void tuneHttpPool() {
        PoolStats totalStats = httpConnectionManager.getTotalStats();

        int available = totalStats.getAvailable();
        int leased = totalStats.getLeased();
        int pending = totalStats.getPending();
        int max = totalStats.getMax();

        double utilization = max > 0 ? (double) leased / max : 0;

        log.debug("HTTP Pool - Max: {}, Leased: {}, Available: {}, Pending: {}, Utilization: {:.2f}%",
                max, leased, available, pending, utilization * 100);

        // Record metrics
        meterRegistry.gauge("http.pool.utilization", utilization);
        meterRegistry.gauge("http.pool.pending", pending);

        // Critical condition - pending requests and high utilization
        if(pending > 0 && utilization > CRITICAL_UTILIZATION_THRESHOLD) {
            int currentMax = httpConnectionManager.getMaxTotal();
            int newMax = Math.min((int) (currentMax * INCREASE_FACTOR), MAX_HTTP_CONNECTIONS);

            if(newMax > currentMax) {
                httpConnectionManager.setMaxTotal(newMax);
                // Also increase per - route limit proportionally
                int newPerRoute = Math.max(20, newMax / 10);
                httpConnectionManager.setDefaultMaxPerRoute(newPerRoute);

                log.warn("Increased HTTP pool size from {} to {} due to pending requests",
                        currentMax, newMax);
            }
        }
        // High utilization
        else if(utilization > HIGH_UTILIZATION_THRESHOLD) {
            int currentMax = httpConnectionManager.getMaxTotal();
            int newMax = Math.min((int) (currentMax * 1.1), MAX_HTTP_CONNECTIONS);

            if(newMax > currentMax) {
                httpConnectionManager.setMaxTotal(newMax);
                log.info("Proactively increased HTTP pool size from {} to {}", currentMax, newMax);
            }
        }
        // Low utilization
        else if(utilization < LOW_UTILIZATION_THRESHOLD && max > MIN_HTTP_CONNECTIONS) {
            int currentMax = httpConnectionManager.getMaxTotal();
            int newMax = Math.max((int) (currentMax * DECREASE_FACTOR), MIN_HTTP_CONNECTIONS);

            if(newMax < currentMax) {
                httpConnectionManager.setMaxTotal(newMax);
                // Adjust per - route limit
                int newPerRoute = Math.max(10, newMax / 10);
                httpConnectionManager.setDefaultMaxPerRoute(newPerRoute);

                log.info("Decreased HTTP pool size from {} to {} due to low utilization",
                        currentMax, newMax);
            }
        }

        // Clean up idle connections if we have too many
        if(available > leased * 2 && available > 10) {
            httpConnectionManager.closeIdleConnections(30, java.util.concurrent.TimeUnit.SECONDS);
            log.debug("Cleaned up idle HTTP connections");
        }
    }

    /**
     * Get current pool statistics.
     */
    public PoolStatistics getPoolStatistics() {
        PoolStatistics stats = new PoolStatistics();

        // Database pool stats
        if(dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            if(poolMXBean != null) {
                stats.setDbPoolMax(hikariDataSource.getMaximumPoolSize());
                stats.setDbPoolActive(poolMXBean.getActiveConnections());
                stats.setDbPoolIdle(poolMXBean.getIdleConnections());
                stats.setDbPoolWaiting(poolMXBean.getThreadsAwaitingConnection());
            }
        }

        // HTTP pool stats
        PoolStats httpStats = httpConnectionManager.getTotalStats();
        stats.setHttpPoolMax(httpStats.getMax());
        stats.setHttpPoolLeased(httpStats.getLeased());
        stats.setHttpPoolAvailable(httpStats.getAvailable());
        stats.setHttpPoolPending(httpStats.getPending());

        return stats;
    }

    public static class PoolStatistics {
        // Database pool
        private int dbPoolMax;
        private int dbPoolActive;
        private int dbPoolIdle;
        private int dbPoolWaiting;

        // HTTP pool
        private int httpPoolMax;
        private int httpPoolLeased;
        private int httpPoolAvailable;
        private int httpPoolPending;

        // Getters and setters
        public int getDbPoolMax() { return dbPoolMax; }
        public void setDbPoolMax(int dbPoolMax) { this.dbPoolMax = dbPoolMax; }

        public int getDbPoolActive() { return dbPoolActive; }
        public void setDbPoolActive(int dbPoolActive) { this.dbPoolActive = dbPoolActive; }

        public int getDbPoolIdle() { return dbPoolIdle; }
        public void setDbPoolIdle(int dbPoolIdle) { this.dbPoolIdle = dbPoolIdle; }

        public int getDbPoolWaiting() { return dbPoolWaiting; }
        public void setDbPoolWaiting(int dbPoolWaiting) { this.dbPoolWaiting = dbPoolWaiting; }

        public int getHttpPoolMax() { return httpPoolMax; }
        public void setHttpPoolMax(int httpPoolMax) { this.httpPoolMax = httpPoolMax; }

        public int getHttpPoolLeased() { return httpPoolLeased; }
        public void setHttpPoolLeased(int httpPoolLeased) { this.httpPoolLeased = httpPoolLeased; }

        public int getHttpPoolAvailable() { return httpPoolAvailable; }
        public void setHttpPoolAvailable(int httpPoolAvailable) { this.httpPoolAvailable = httpPoolAvailable; }

        public int getHttpPoolPending() { return httpPoolPending; }
        public void setHttpPoolPending(int httpPoolPending) { this.httpPoolPending = httpPoolPending; }

        public double getDbPoolUtilization() {
            return dbPoolMax > 0 ? (double) dbPoolActive / dbPoolMax : 0;
        }

        public double getHttpPoolUtilization() {
            return httpPoolMax > 0 ? (double) httpPoolLeased / httpPoolMax : 0;
        }
    }
}
