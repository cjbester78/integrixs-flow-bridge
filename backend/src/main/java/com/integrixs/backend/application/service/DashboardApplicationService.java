package com.integrixs.backend.application.service;

import com.integrixs.backend.domain.service.MetricsAggregatorService;
import com.integrixs.backend.domain.service.StatisticsCalculatorService;
import com.integrixs.shared.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for dashboard operations
 * Orchestrates metrics and statistics calculation
 */
@Service
public class DashboardApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DashboardApplicationService.class);


    private final MetricsAggregatorService metricsAggregator;
    private final StatisticsCalculatorService statisticsCalculator;

    public DashboardApplicationService(MetricsAggregatorService metricsAggregator,
                                     StatisticsCalculatorService statisticsCalculator) {
        this.metricsAggregator = metricsAggregator;
        this.statisticsCalculator = statisticsCalculator;
    }

    /**
     * Get comprehensive dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats(String businessComponentId) {
        log.debug("Getting dashboard stats for business component: {}", businessComponentId);

        // Calculate metrics based on business component filter
        int activeIntegrations;
        long messagesToday;
        double successRate;
        long avgResponseTime;

        if(businessComponentId != null) {
            UUID componentId = UUID.fromString(businessComponentId);
            activeIntegrations = metricsAggregator.countActiveFlowsByBusinessComponent(componentId);
            messagesToday = statisticsCalculator.countMessagesTodayForBusinessComponent(componentId);
            successRate = statisticsCalculator.calculateSuccessRateTodayForBusinessComponent(componentId);
            avgResponseTime = statisticsCalculator.calculateAverageResponseTimeForBusinessComponent(componentId);
        } else {
            activeIntegrations = metricsAggregator.countActiveFlows();
            messagesToday = statisticsCalculator.countMessagesToday();
            successRate = statisticsCalculator.calculateSuccessRateToday();
            avgResponseTime = statisticsCalculator.calculateAverageResponseTime();
        }

        // Get additional metrics
        int totalFlows = metricsAggregator.countTotalFlows();
        int errorFlows = metricsAggregator.countFlowsWithErrors(5); // Flows with > 5 errors
        double uptimePercentage = statisticsCalculator.calculateUptimePercentage();

        // Ensure response time is reasonable
        if(avgResponseTime == 0) {
            avgResponseTime = metricsAggregator.calculateAverageProcessingTime();
        }

        log.info("Dashboard stats - Active flows: {}, Messages today: {}, Success rate: {}%, Avg response: {}ms",
            activeIntegrations, messagesToday, String.format("%.2f", successRate), avgResponseTime);

        return DashboardStatsDTO.builder()
            .activeIntegrations(activeIntegrations)
            .integrationFlowsToday(messagesToday)
            .successRate(successRate)
            .avgResponseTime(avgResponseTime)
            .totalFlows(totalFlows)
            .errorFlows(errorFlows)
            .uptimePercentage(uptimePercentage)
            .build();
    }

    /**
     * Get metrics for dashboard(alias for stats)
     * Frontend uses both endpoints interchangeably
     */
    public DashboardStatsDTO getDashboardMetrics(String businessComponentId) {
        return getDashboardStats(businessComponentId);
    }
}
