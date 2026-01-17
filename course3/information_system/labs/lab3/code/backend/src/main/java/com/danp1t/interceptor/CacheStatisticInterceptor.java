package com.danp1t.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import java.util.logging.Logger;

@Interceptor
@CacheStatistic
public class CacheStatisticInterceptor {

    private static final Logger logger = Logger.getLogger(CacheStatisticInterceptor.class.getName());

    @Inject
    private SessionFactory sessionFactory;

    private Statistics statistics;
    private boolean statisticsEnabled = false;

    @PostConstruct
    public void init() {
        // Проверяем, включена ли статистика в настройках
        statistics = sessionFactory.getStatistics();
        statisticsEnabled = statistics.isStatisticsEnabled();

        if (!statisticsEnabled) {
            logger.info("Hibernate statistics are disabled. Enable them with 'hibernate.generate_statistics=true'");
        }
    }

    @AroundInvoke
    public Object logCacheStatistics(InvocationContext context) throws Exception {
        CacheStatistic annotation = getCacheStatisticAnnotation(context);

        if (annotation == null || !annotation.enabled() || !statisticsEnabled) {
            return context.proceed();
        }

        // Сохраняем начальные значения
        long secondLevelCacheHitCountBefore = statistics.getSecondLevelCacheHitCount();
        long secondLevelCacheMissCountBefore = statistics.getSecondLevelCacheMissCount();
        long queryCacheHitCountBefore = statistics.getQueryCacheHitCount();
        long queryCacheMissCountBefore = statistics.getQueryCacheMissCount();

        // Выполняем метод
        Object result = context.proceed();

        // Вычисляем разницу
        long secondLevelCacheHitCountAfter = statistics.getSecondLevelCacheHitCount();
        long secondLevelCacheMissCountAfter = statistics.getSecondLevelCacheMissCount();
        long queryCacheHitCountAfter = statistics.getQueryCacheHitCount();
        long queryCacheMissCountAfter = statistics.getQueryCacheMissCount();

        long secondLevelCacheHits = secondLevelCacheHitCountAfter - secondLevelCacheHitCountBefore;
        long secondLevelCacheMisses = secondLevelCacheMissCountAfter - secondLevelCacheMissCountBefore;
        long queryCacheHits = queryCacheHitCountAfter - queryCacheHitCountBefore;
        long queryCacheMisses = queryCacheMissCountAfter - queryCacheMissCountBefore;

        // Логируем статистику
        logStatistics(
                context.getMethod().getName(),
                secondLevelCacheHits,
                secondLevelCacheMisses,
                queryCacheHits,
                queryCacheMisses
        );

        return result;
    }

    private CacheStatistic getCacheStatisticAnnotation(InvocationContext context) {
        // Проверяем аннотацию на методе
        CacheStatistic methodAnnotation = context.getMethod().getAnnotation(CacheStatistic.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // Проверяем аннотацию на классе
        return context.getTarget().getClass().getAnnotation(CacheStatistic.class);
    }

    private void logStatistics(String methodName, long l2Hits, long l2Misses, long queryHits, long queryMisses) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n=== Cache Statistics for method '").append(methodName).append("' ===\n");
        logMessage.append("L2 Cache Hits: ").append(l2Hits).append("\n");
        logMessage.append("L2 Cache Misses: ").append(l2Misses).append("\n");

        if (l2Hits + l2Misses > 0) {
            double l2HitRatio = (double) l2Hits / (l2Hits + l2Misses) * 100;
            logMessage.append(String.format("L2 Cache Hit Ratio: %.2f%%\n", l2HitRatio));
        }

        logMessage.append("Query Cache Hits: ").append(queryHits).append("\n");
        logMessage.append("Query Cache Misses: ").append(queryMisses).append("\n");

        if (queryHits + queryMisses > 0) {
            double queryHitRatio = (double) queryHits / (queryHits + queryMisses) * 100;
            logMessage.append(String.format("Query Cache Hit Ratio: %.2f%%\n", queryHitRatio));
        }

        logMessage.append("=========================================");

        logger.info(logMessage.toString());
    }

    @PreDestroy
    public void destroy() {
        // При желании можно залогировать общую статистику при завершении
        if (statisticsEnabled) {
            logOverallStatistics();
        }
    }

    private void logOverallStatistics() {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n=== Overall Cache Statistics ===\n");
        logMessage.append("Total L2 Cache Hits: ").append(statistics.getSecondLevelCacheHitCount()).append("\n");
        logMessage.append("Total L2 Cache Misses: ").append(statistics.getSecondLevelCacheMissCount()).append("\n");

        long totalL2Requests = statistics.getSecondLevelCacheHitCount() + statistics.getSecondLevelCacheMissCount();
        if (totalL2Requests > 0) {
            double l2HitRatio = (double) statistics.getSecondLevelCacheHitCount() / totalL2Requests * 100;
            logMessage.append(String.format("Overall L2 Cache Hit Ratio: %.2f%%\n", l2HitRatio));
        }

        logMessage.append("Total Query Cache Hits: ").append(statistics.getQueryCacheHitCount()).append("\n");
        logMessage.append("Total Query Cache Misses: ").append(statistics.getQueryCacheMissCount()).append("\n");

        long totalQueryRequests = statistics.getQueryCacheHitCount() + statistics.getQueryCacheMissCount();
        if (totalQueryRequests > 0) {
            double queryHitRatio = (double) statistics.getQueryCacheHitCount() / totalQueryRequests * 100;
            logMessage.append(String.format("Overall Query Cache Hit Ratio: %.2f%%\n", queryHitRatio));
        }

        // Статистика по регионам кэша
        logMessage.append("\n=== Cache Region Statistics ===\n");
        String[] secondLevelCacheRegionNames = statistics.getSecondLevelCacheRegionNames();
        for (String region : secondLevelCacheRegionNames) {
            logMessage.append("Region: ").append(region).append("\n");
            logMessage.append("  - Put Count: ").append(statistics.getSecondLevelCachePutCount()).append("\n");
            logMessage.append("  - Hit Count: ").append(statistics.getSecondLevelCacheHitCount()).append("\n");
            logMessage.append("  - Miss Count: ").append(statistics.getSecondLevelCacheMissCount()).append("\n");
        }

        logMessage.append("==================================");

        logger.info(logMessage.toString());
    }
}