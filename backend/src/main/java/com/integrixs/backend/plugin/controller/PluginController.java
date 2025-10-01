package com.integrixs.backend.plugin.controller;

import com.integrixs.backend.plugin.api.*;
import com.integrixs.backend.plugin.dto.*;
import com.integrixs.backend.plugin.registry.PluginRegistry;
import com.integrixs.backend.plugin.service.PluginService;
import com.integrixs.backend.plugin.documentation.PluginDocumentationGenerator;
import com.integrixs.backend.plugin.monitoring.PluginPerformanceMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for plugin management
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private static final Logger log = LoggerFactory.getLogger(PluginController.class);


    private final PluginRegistry pluginRegistry;
    private final PluginService pluginService;
    private final PluginPerformanceMonitor performanceMonitor;
    private final PluginDocumentationGenerator documentationGenerator = new PluginDocumentationGenerator();

    public PluginController(PluginRegistry pluginRegistry,
                           PluginService pluginService,
                           PluginPerformanceMonitor performanceMonitor) {
        this.pluginRegistry = pluginRegistry;
        this.pluginService = pluginService;
        this.performanceMonitor = performanceMonitor;
    }

    @GetMapping
    public ResponseEntity<List<PluginDto>> getAllPlugins() {
        List<PluginDto> plugins = pluginRegistry.getRegisteredPlugins().values().stream()
                .map(metadata -> PluginDto.fromMetadata(metadata))
                .collect(Collectors.toList());

        return ResponseEntity.ok(plugins);
    }

    @GetMapping("/ {pluginId}")
    public ResponseEntity<PluginDetailsDto> getPlugin(@PathVariable String pluginId) {
        AdapterMetadata metadata = pluginRegistry.getRegisteredPlugins().get(pluginId);
        if(metadata == null) {
            return ResponseEntity.notFound().build();
        }

        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ResponseEntity.notFound().build();
        }

        PluginDetailsDto details = PluginDetailsDto.builder()
                .metadata(metadata)
                .configurationSchema(plugin.getConfigurationSchema())
                .health(plugin.checkHealth())
                .isInitialized(pluginRegistry.isInitialized(pluginId))
                .build();

        return ResponseEntity.ok(details);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResultDto> uploadPlugin(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "validate", defaultValue = "true") boolean validate) {

        try {
            UploadResultDto result = pluginService.uploadPlugin(file, validate);
            return ResponseEntity.status(
                result.isSuccessful() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
           ).body(result);
        } catch(Exception e) {
            log.error("Failed to upload plugin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UploadResultDto.failure("Failed to upload plugin: " + e.getMessage()));
        }
    }

    @PostMapping("/ {pluginId}/initialize")
    public ResponseEntity<InitializationResultDto> initializePlugin(
            @PathVariable String pluginId,
            @Valid @RequestBody Map<String, Object> configuration) {

        try {
            pluginService.initializePlugin(pluginId, configuration);
            return ResponseEntity.ok(InitializationResultDto.success(pluginId));
        } catch(Exception e) {
            log.error("Failed to initialize plugin {}", pluginId, e);
            return ResponseEntity.badRequest()
                    .body(InitializationResultDto.failure(pluginId, e.getMessage()));
        }
    }

    @DeleteMapping("/ {pluginId}")
    public ResponseEntity<Void> unregisterPlugin(@PathVariable String pluginId) {
        if(!pluginRegistry.getRegisteredPlugins().containsKey(pluginId)) {
            return ResponseEntity.notFound().build();
        }

        try {
            pluginService.unregisterPlugin(pluginId);
            return ResponseEntity.noContent().build();
        } catch(Exception e) {
            log.error("Failed to unregister plugin {}", pluginId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ {pluginId}/test - connection")
    public ResponseEntity<ConnectionTestResult> testConnection(
            @PathVariable String pluginId,
            @RequestParam AdapterPlugin.Direction direction,
            @Valid @RequestBody(required = false) Map<String, Object> configuration) {

        try {
            ConnectionTestResult result = pluginService.testConnection(pluginId, direction, configuration);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            log.error("Failed to test connection for plugin {}", pluginId, e);
            return ResponseEntity.ok(
                ConnectionTestResult.failure("Test failed", e.getMessage())
           );
        }
    }

    @GetMapping("/ {pluginId}/health")
    public ResponseEntity<HealthStatus> checkHealth(@PathVariable String pluginId) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(plugin.checkHealth());
    }

    @GetMapping("/ {pluginId}/configuration - schema")
    public ResponseEntity<ConfigurationSchema> getConfigurationSchema(@PathVariable String pluginId) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(plugin.getConfigurationSchema());
    }

    @PostMapping("/ {pluginId}/validate - configuration")
    public ResponseEntity<ValidationResultDto> validateConfiguration(
            @PathVariable String pluginId,
            @Valid @RequestBody Map<String, Object> configuration) {

        try {
            ValidationResultDto result = pluginService.validateConfiguration(pluginId, configuration);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            log.error("Failed to validate configuration for plugin {}", pluginId, e);
            return ResponseEntity.badRequest()
                    .body(ValidationResultDto.error("Validation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getPluginCategories() {
        List<String> categories = pluginRegistry.getRegisteredPlugins().values().stream()
                .map(AdapterMetadata::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PluginDto>> searchPlugins(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags) {

        List<PluginDto> plugins = pluginService.searchPlugins(query, category, tags);
        return ResponseEntity.ok(plugins);
    }

    @GetMapping("/ {pluginId}/documentation")
    public ResponseEntity<String> getDocumentation(
            @PathVariable String pluginId,
            @RequestParam(defaultValue = "markdown") String format) {

        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String documentation;
            MediaType contentType;

            switch(format.toLowerCase()) {
                case "html":
                    documentation = documentationGenerator.generateHTML(plugin);
                    contentType = MediaType.TEXT_HTML;
                    break;
                case "openapi":
                case "swagger":
                    documentation = documentationGenerator.generateOpenAPISpec(plugin);
                    contentType = MediaType.APPLICATION_JSON;
                    break;
                case "markdown":
                case "md":
                default:
                    documentation = documentationGenerator.generateMarkdown(plugin);
                    contentType = MediaType.TEXT_MARKDOWN;
                    break;
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(documentation);

        } catch(Exception e) {
            log.error("Failed to generate documentation for plugin {}", pluginId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate documentation: " + e.getMessage());
        }
    }

    @GetMapping("/ {pluginId}/metrics")
    public ResponseEntity<Object> getPluginMetrics(@PathVariable String pluginId) {
        var metrics = performanceMonitor.getMetrics(pluginId);
        if(metrics == null) {
            return ResponseEntity.notFound().build();
        }

        // Calculate trends by comparing last 5 minutes with previous 5 minutes
        var last5Min = performanceMonitor.getStatistics(pluginId, java.time.Duration.ofMinutes(5));
        var prev5Min = performanceMonitor.getStatistics(pluginId, java.time.Duration.ofMinutes(10));

        // Calculate trend percentages
        double messagesTrend = calculateTrendPercentage(
            last5Min.getSampleCount(),
            prev5Min.getSampleCount() - last5Min.getSampleCount()
       );

        double successRateTrend = calculateTrendPercentage(
            last5Min.getSuccessRate(),
            prev5Min.getSuccessRate()
       );

        double responseTimeTrend = calculateTrendPercentage(
            last5Min.getAverageProcessingTime(),
            prev5Min.getAverageProcessingTime()
       );

        // For errors, we need to get error count from the time windows
        long recentErrors = last5Min.getSampleCount() - last5Min.getSuccessCount();
        long previousErrors = (prev5Min.getSampleCount() - last5Min.getSampleCount()) -
                             (prev5Min.getSuccessCount() - last5Min.getSuccessCount());
        double errorsTrend = calculateTrendPercentage(recentErrors, previousErrors);

        return ResponseEntity.ok(Map.of(
            "pluginId", pluginId,
            "messagesProcessed", metrics.getMessagesProcessed().get(),
            "successRate", metrics.getSuccessRate(),
            "averageProcessingTime", metrics.getAverageProcessingTime(),
            "errors", metrics.getErrors().get(),
            "errorSummary", metrics.getErrorCounts(),
            "trend", Map.of(
                "messages", messagesTrend,
                "successRate", successRateTrend,
                "responseTime", responseTimeTrend,
                "errors", errorsTrend
           ),
            "recentStats", Map.of(
                "last5Minutes", last5Min,
                "sampleCount", last5Min.getSampleCount()
           )
       ));
    }

    /**
     * Calculate trend percentage between current and previous values
     * Positive percentage indicates increase, negative indicates decrease
     */
    private double calculateTrendPercentage(double current, double previous) {
        if(previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return((current - previous) / previous) * 100.0;
    }

    @GetMapping("/ {pluginId}/performance - report")
    public ResponseEntity<Object> getPerformanceReport(@PathVariable String pluginId) {
        var report = performanceMonitor.generateReport(pluginId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        var allMetrics = performanceMonitor.getAllMetrics();

        Map<String, Object> summary = new HashMap<>();
        for(Map.Entry<String, PluginPerformanceMonitor.PluginMetrics> entry : allMetrics.entrySet()) {
            var metrics = entry.getValue();
            summary.put(entry.getKey(), Map.of(
                "messagesProcessed", metrics.getMessagesProcessed().get(),
                "successRate", metrics.getSuccessRate(),
                "averageProcessingTime", metrics.getAverageProcessingTime(),
                "errors", metrics.getErrors().get()
           ));
        }

        return ResponseEntity.ok(summary);
    }
}
