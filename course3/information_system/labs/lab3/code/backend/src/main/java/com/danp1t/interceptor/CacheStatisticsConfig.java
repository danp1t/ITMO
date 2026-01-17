package com.danp1t.interceptor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import java.util.logging.Logger;

@ApplicationScoped
public class CacheStatisticsConfig {

    private static final Logger logger = Logger.getLogger(CacheStatisticsConfig.class.getName());

    @Inject
    private SessionFactory sessionFactory;

    private Statistics statistics;

    public void enableStatistics() {
        if (sessionFactory != null) {
            statistics = sessionFactory.getStatistics();
            statistics.setStatisticsEnabled(true);
            logger.info("Hibernate cache statistics enabled");
        }
    }

    public void disableStatistics() {
        if (statistics != null) {
            statistics.setStatisticsEnabled(false);
            logger.info("Hibernate cache statistics disabled");
        }
    }

    public boolean isStatisticsEnabled() {
        return statistics != null && statistics.isStatisticsEnabled();
    }

    public void resetStatistics() {
        if (statistics != null) {
            statistics.clear();
            logger.info("Hibernate cache statistics reset");
        }
    }

    public String getStatisticsSummary() {
        if (statistics == null || !statistics.isStatisticsEnabled()) {
            return "Statistics are disabled";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== Cache Statistics Summary ===\n");
        summary.append("L2 Cache Hits: ").append(statistics.getSecondLevelCacheHitCount()).append("\n");
        summary.append("L2 Cache Misses: ").append(statistics.getSecondLevelCacheMissCount()).append("\n");
        summary.append("Query Cache Hits: ").append(statistics.getQueryCacheHitCount()).append("\n");
        summary.append("Query Cache Misses: ").append(statistics.getQueryCacheMissCount()).append("\n");

        return summary.toString();
    }
}