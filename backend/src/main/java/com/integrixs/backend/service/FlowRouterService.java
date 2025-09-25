package com.integrixs.backend.service;

import com.integrixs.backend.service.FlowContextService.FlowContext;
import com.integrixs.data.model.RouteCondition.SourceType;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FlowRouter;
import com.integrixs.data.sql.repository.FlowRouterSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.xpath.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Flow Router Service - Implements various routing patterns for message flows
 * Supports content - based routing, multicast, dynamic routing, and more
 */
@Service
public class FlowRouterService {

    private static final Logger logger = LoggerFactory.getLogger(FlowRouterService.class);

    @Autowired
    private FlowContextService contextService;


    @Autowired
    private FlowRouterSqlRepository routerRepository;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private EnhancedSagaTransactionService sagaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Router registry
    private final Map<String, RouterInstance> activeRouters = new ConcurrentHashMap<>();

    /**
     * Create a content - based router
     */
    public FlowRouter createContentBasedRouter(String flowId, String routerName,
                                              String extractionPath, SourceType sourceType,
                                              Map<String, RouterTarget> routeMap) {

        FlowRouter router = new FlowRouter();
        router.setFlowId(UUID.fromString(flowId));
        router.setName(routerName);
        router.setRouterType(FlowRouter.RouterType.CONTENT_BASED);
        router.setConfiguration(buildContentRouterConfig(extractionPath, sourceType, routeMap));
        router.setActive(true);

        router = routerRepository.save(router);

        // Register router instance
        registerRouter(router);

        logger.info("Created content - based router {} for flow {}", routerName, flowId);
        return router;
    }

    /**
     * Create a multicast router(sends to multiple targets)
     */
    public FlowRouter createMulticastRouter(String flowId, String routerName,
                                          List<RouterTarget> targets,
                                          boolean parallel) {

        FlowRouter router = new FlowRouter();
        router.setFlowId(UUID.fromString(flowId));
        router.setName(routerName);
        router.setRouterType(FlowRouter.RouterType.MULTICAST);
        router.setConfiguration(buildMulticastConfig(targets, parallel));
        router.setActive(true);

        router = routerRepository.save(router);
        registerRouter(router);

        logger.info("Created multicast router {} for flow {}", routerName, flowId);
        return router;
    }

    /**
     * Create a dynamic router(routes based on runtime logic)
     */
    public FlowRouter createDynamicRouter(String flowId, String routerName,
                                         String routingExpression) {

        FlowRouter router = new FlowRouter();
        router.setFlowId(UUID.fromString(flowId));
        router.setName(routerName);
        router.setRouterType(FlowRouter.RouterType.DYNAMIC);
        router.setConfiguration(buildDynamicConfig(routingExpression));
        router.setActive(true);

        router = routerRepository.save(router);
        registerRouter(router);

        logger.info("Created dynamic router {} for flow {}", routerName, flowId);
        return router;
    }

    /**
     * Create a splitter router(splits message into parts)
     */
    public FlowRouter createSplitterRouter(String flowId, String routerName,
                                         String splitExpression,
                                         SplitStrategy strategy) {

        FlowRouter router = new FlowRouter();
        router.setFlowId(UUID.fromString(flowId));
        router.setName(routerName);
        router.setRouterType(FlowRouter.RouterType.SPLITTER);
        router.setConfiguration(buildSplitterConfig(splitExpression, strategy));
        router.setActive(true);

        router = routerRepository.save(router);
        registerRouter(router);

        logger.info("Created splitter router {} for flow {}", routerName, flowId);
        return router;
    }

    /**
     * Create an aggregator router(combines multiple messages)
     */
    public FlowRouter createAggregatorRouter(String flowId, String routerName,
                                           String correlationExpression,
                                           AggregationStrategy strategy,
                                           int completionSize,
                                           long timeoutMs) {

        FlowRouter router = new FlowRouter();
        router.setFlowId(UUID.fromString(flowId));
        router.setName(routerName);
        router.setRouterType(FlowRouter.RouterType.AGGREGATOR);
        router.setConfiguration(buildAggregatorConfig(correlationExpression, strategy,
                                                     completionSize, timeoutMs));
        router.setActive(true);

        router = routerRepository.save(router);
        registerRouter(router);

        logger.info("Created aggregator router {} for flow {}", routerName, flowId);
        return router;
    }

    /**
     * Route a message through a router
     */
    public CompletableFuture<RoutingResult> route(String routerId, FlowContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RouterInstance instance = activeRouters.get(routerId);
                if(instance == null) {
                    // Try to load from database
                    FlowRouter router = routerRepository.findById(UUID.fromString(routerId))
                        .orElseThrow(() -> new IllegalArgumentException("Router not found: " + routerId));

                    instance = registerRouter(router);
                }

                if(!instance.isActive()) {
                    return RoutingResult.inactive();
                }

                return executeRouting(instance, context);

            } catch(Exception e) {
                logger.error("Routing failed for router {}", routerId, e);
                return RoutingResult.error(e.getMessage());
            }
        });
    }

    /**
     * Execute routing based on router type
     */
    private RoutingResult executeRouting(RouterInstance instance, FlowContext context) {
        FlowRouter router = instance.getRouter();

        switch(router.getRouterType()) {
            case CONTENT_BASED:
                return executeContentBasedRouting(instance, context);

            case MULTICAST:
                return executeMulticastRouting(instance, context);

            case DYNAMIC:
                return executeDynamicRouting(instance, context);

            case SPLITTER:
                return executeSplitterRouting(instance, context);

            case AGGREGATOR:
                return executeAggregatorRouting(instance, context);

            case CHOICE:
                return executeChoiceRouting(instance, context);

            case FILTER:
                return executeFilterRouting(instance, context);

            default:
                return RoutingResult.error("Unknown router type: " + router.getRouterType());
        }
    }

    /**
     * Execute content - based routing
     */
    private RoutingResult executeContentBasedRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            String extractionPath = (String) config.get("extractionPath");
            SourceType sourceType = SourceType.valueOf((String) config.get("sourceType"));
            Map<String, Map<String, Object>> routes = (Map<String, Map<String, Object>>) config.get("routes");

            // Extract value
            String value = extractValue(context, extractionPath, sourceType);

            if(value != null && routes.containsKey(value)) {
                Map<String, Object> targetConfig = routes.get(value);
                RouterTarget target = RouterTarget.fromMap(targetConfig);
                return RoutingResult.success(Collections.singletonList(target));
            }

            // Check for default route
            if(routes.containsKey("_default")) {
                Map<String, Object> targetConfig = routes.get("_default");
                RouterTarget target = RouterTarget.fromMap(targetConfig);
                return RoutingResult.success(Collections.singletonList(target));
            }

            return RoutingResult.noMatch();

        } catch(Exception e) {
            logger.error("Content - based routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute multicast routing
     */
    private RoutingResult executeMulticastRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            List<Map<String, Object>> targetConfigs = (List<Map<String, Object>>) config.get("targets");
            boolean parallel = (boolean) config.getOrDefault("parallel", false);

            List<RouterTarget> targets = targetConfigs.stream()
                .map(RouterTarget::fromMap)
                .collect(Collectors.toList());

            if(parallel) {
                // Execute targets in parallel using saga
                List<SagaTransactionService.SagaStepDefinition> parallelSteps = targets.stream()
                    .map(target -> new SagaTransactionService.SagaStepDefinition(
                        target.getTargetId(),
                        "ROUTE_TARGET",
                        0,
                        Map.of("target", target)
                   ))
                    .collect(Collectors.toList());

                context.setVariable("parallelTargets", targets);
            }

            return RoutingResult.success(targets);

        } catch(Exception e) {
            logger.error("Multicast routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute dynamic routing
     */
    private RoutingResult executeDynamicRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            String routingExpression = (String) config.get("expression");

            // Evaluate expression to get target
            String targetId = contextService.evaluateExpression(context, routingExpression);

            if(targetId != null && !targetId.isEmpty()) {
                RouterTarget target = new RouterTarget(targetId, TargetType.FLOW);
                return RoutingResult.success(Collections.singletonList(target));
            }

            return RoutingResult.noMatch();

        } catch(Exception e) {
            logger.error("Dynamic routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute splitter routing
     */
    private RoutingResult executeSplitterRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            String splitExpression = (String) config.get("expression");
            SplitStrategy strategy = SplitStrategy.valueOf((String) config.get("strategy"));

            List<Object> splitParts = new ArrayList<>();

            switch(strategy) {
                case XPATH:
                    splitParts = splitByXPath(context.getPayload(), splitExpression);
                    break;

                case JSONPATH:
                    splitParts = splitByJsonPath(context.getPayload(), splitExpression);
                    break;

                case DELIMITER:
                    splitParts = splitByDelimiter(context.getPayload(), splitExpression);
                    break;

                case LINE:
                    splitParts = splitByLine(context.getPayload());
                    break;
            }

            // Store split parts in context
            context.setVariable("splitParts", splitParts);
            context.setVariable("splitCount", splitParts.size());

            // Create a target for each split part
            List<RouterTarget> targets = new ArrayList<>();
            for(int i = 0; i < splitParts.size(); i++) {
                RouterTarget target = new RouterTarget(
                    "split_processor_" + i,
                    TargetType.PROCESSOR
               );
                target.getMetadata().put("splitIndex", i);
                target.getMetadata().put("splitPart", splitParts.get(i));
                targets.add(target);
            }

            return RoutingResult.success(targets);

        } catch(Exception e) {
            logger.error("Splitter routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute aggregator routing
     */
    private RoutingResult executeAggregatorRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            String correlationExpression = (String) config.get("correlationExpression");
            AggregationStrategy strategy = AggregationStrategy.valueOf((String) config.get("strategy"));
            int completionSize = (int) config.get("completionSize");
            long timeoutMs = (long) config.get("timeoutMs");

            // Extract correlation ID
            String correlationId = contextService.evaluateExpression(context, correlationExpression);

            // Get or create aggregation group
            AggregationGroup group = instance.getAggregationGroup(correlationId);
            group.addMessage(context.getPayload());

            // Check if aggregation is complete
            if(group.isComplete(completionSize, timeoutMs)) {
                Object aggregatedResult = aggregate(group.getMessages(), strategy);
                context.setPayload(aggregatedResult);

                // Clear the group
                instance.removeAggregationGroup(correlationId);

                // Route to next step
                RouterTarget target = new RouterTarget("aggregation_complete", TargetType.PROCESSOR);
                return RoutingResult.success(Collections.singletonList(target));
            }

            // Not complete yet, hold the message
            return RoutingResult.hold();

        } catch(Exception e) {
            logger.error("Aggregator routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute choice routing(if - else)
     */
    private RoutingResult executeChoiceRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            List<Map<String, Object>> choices = (List<Map<String, Object>>) config.get("choices");

            for(Map<String, Object> choice : choices) {
                String condition = (String) choice.get("condition");

                if(contextService.evaluateCondition(context, condition)) {
                    Map<String, Object> targetConfig = (Map<String, Object>) choice.get("target");
                    RouterTarget target = RouterTarget.fromMap(targetConfig);
                    return RoutingResult.success(Collections.singletonList(target));
                }
            }

            // No condition matched, check for otherwise
            Map<String, Object> otherwiseTarget = (Map<String, Object>) config.get("otherwise");
            if(otherwiseTarget != null) {
                RouterTarget target = RouterTarget.fromMap(otherwiseTarget);
                return RoutingResult.success(Collections.singletonList(target));
            }

            return RoutingResult.noMatch();

        } catch(Exception e) {
            logger.error("Choice routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    /**
     * Execute filter routing
     */
    private RoutingResult executeFilterRouting(RouterInstance instance, FlowContext context) {
        try {
            Map<String, Object> config = parseConfiguration(instance.getRouter().getConfiguration());
            String filterExpression = (String) config.get("expression");

            boolean pass = contextService.evaluateCondition(context, filterExpression);

            if(pass) {
                Map<String, Object> targetConfig = (Map<String, Object>) config.get("target");
                RouterTarget target = RouterTarget.fromMap(targetConfig);
                return RoutingResult.success(Collections.singletonList(target));
            } else {
                // Message filtered out
                return RoutingResult.filtered();
            }

        } catch(Exception e) {
            logger.error("Filter routing failed", e);
            return RoutingResult.error(e.getMessage());
        }
    }

    // Helper methods

    private RouterInstance registerRouter(FlowRouter router) {
        RouterInstance instance = new RouterInstance(router);
        activeRouters.put(router.getId().toString(), instance);
        return instance;
    }

    private String extractValue(FlowContext context, String path, SourceType sourceType) {
        switch(sourceType) {
            case HEADER:
                return context.getHeader(path);
            case VARIABLE:
                Object var = context.getVariable(path);
                return var != null ? var.toString() : null;
            case XPATH:
                if(context.getPayload() instanceof String) {
                    return contextService.extractValueFromXml((String) context.getPayload(), path);
                }
                return null;
            case JSONPATH:
                if(context.getPayload() instanceof String) {
                    return contextService.extractValueFromJson((String) context.getPayload(), path);
                }
                return null;
            default:
                return null;
        }
    }

    private List<Object> splitByXPath(Object payload, String xpath) throws Exception {
        if(!(payload instanceof String)) return Collections.emptyList();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader((String) payload)));

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);

        List<Object> parts = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++) {
            parts.add(nodeToString(nodes.item(i)));
        }

        return parts;
    }

    private List<Object> splitByJsonPath(Object payload, String jsonPath) throws Exception {
        if(!(payload instanceof String)) return Collections.emptyList();

        JsonNode root = objectMapper.readTree((String) payload);
        JsonNode result = root.at(jsonPath);

        List<Object> parts = new ArrayList<>();
        if(result.isArray()) {
            result.forEach(node -> parts.add(node.toString()));
        } else {
            parts.add(result.toString());
        }

        return parts;
    }

    private List<Object> splitByDelimiter(Object payload, String delimiter) {
        if(!(payload instanceof String)) return Collections.emptyList();

        return Arrays.asList((Object[]) ((String) payload).split(delimiter));
    }

    private List<Object> splitByLine(Object payload) {
        return splitByDelimiter(payload, "\n");
    }

    private Object aggregate(List<Object> messages, AggregationStrategy strategy) {
        switch(strategy) {
            case CONCAT:
                return messages.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining());

            case LIST:
                return messages;

            case XML_COMBINE:
                // Combine XML documents
                return combineXmlDocuments(messages);

            case JSON_ARRAY:
                // Create JSON array
                return objectMapper.valueToTree(messages).toString();

            default:
                return messages;
        }
    }

    private String nodeToString(Node node) throws Exception {
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(node),
            new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }

    private String combineXmlDocuments(List<Object> messages) {
        // Simple XML combination - wrap all in root element
        StringBuilder combined = new StringBuilder("<aggregated>");
        for(Object msg : messages) {
            combined.append(msg.toString());
        }
        combined.append("</aggregated>");
        return combined.toString();
    }

    private Map<String, Object> parseConfiguration(String config) throws Exception {
        return objectMapper.readValue(config, Map.class);
    }

    private String buildContentRouterConfig(String extractionPath, SourceType sourceType,
                                           Map<String, RouterTarget> routeMap) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("extractionPath", extractionPath);
            config.put("sourceType", sourceType.name());

            Map<String, Map<String, Object>> routes = new HashMap<>();
            routeMap.forEach((key, target) -> routes.put(key, target.toMap()));
            config.put("routes", routes);

            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            throw new RuntimeException("Failed to build config", e);
        }
    }

    private String buildMulticastConfig(List<RouterTarget> targets, boolean parallel) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("targets", targets.stream().map(RouterTarget::toMap).collect(Collectors.toList()));
            config.put("parallel", parallel);
            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            throw new RuntimeException("Failed to build config", e);
        }
    }

    private String buildDynamicConfig(String expression) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("expression", expression);
            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            throw new RuntimeException("Failed to build config", e);
        }
    }

    private String buildSplitterConfig(String expression, SplitStrategy strategy) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("expression", expression);
            config.put("strategy", strategy.name());
            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            throw new RuntimeException("Failed to build config", e);
        }
    }

    private String buildAggregatorConfig(String correlationExpression, AggregationStrategy strategy,
                                        int completionSize, long timeoutMs) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("correlationExpression", correlationExpression);
            config.put("strategy", strategy.name());
            config.put("completionSize", completionSize);
            config.put("timeoutMs", timeoutMs);
            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            throw new RuntimeException("Failed to build config", e);
        }
    }

    // Supporting classes

    /**
     * Router instance with runtime state
     */
    private static class RouterInstance {
        private final FlowRouter router;
        private final Map<String, AggregationGroup> aggregationGroups = new ConcurrentHashMap<>();

        public RouterInstance(FlowRouter router) {
            this.router = router;
        }

        public FlowRouter getRouter() { return router; }
        public boolean isActive() { return router.isActive(); }

        public AggregationGroup getAggregationGroup(String correlationId) {
            return aggregationGroups.computeIfAbsent(correlationId, k -> new AggregationGroup());
        }

        public void removeAggregationGroup(String correlationId) {
            aggregationGroups.remove(correlationId);
        }
    }

    /**
     * Aggregation group for collecting related messages
     */
    private static class AggregationGroup {
        private final List<Object> messages = Collections.synchronizedList(new ArrayList<>());
        private final long createdAt = System.currentTimeMillis();

        public void addMessage(Object message) {
            messages.add(message);
        }

        public List<Object> getMessages() {
            return new ArrayList<>(messages);
        }

        public boolean isComplete(int targetSize, long timeoutMs) {
            if(messages.size() >= targetSize) return true;
            return(System.currentTimeMillis() - createdAt) > timeoutMs;
        }
    }

    /**
     * Router target
     */
    public static class RouterTarget {
        private final String targetId;
        private final TargetType type;
        private final Map<String, Object> metadata = new HashMap<>();

        public RouterTarget(String targetId, TargetType type) {
            this.targetId = targetId;
            this.type = type;
        }

        public String getTargetId() { return targetId; }
        public TargetType getType() { return type; }
        public Map<String, Object> getMetadata() { return metadata; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("targetId", targetId);
            map.put("type", type.name());
            map.put("metadata", metadata);
            return map;
        }

        public static RouterTarget fromMap(Map<String, Object> map) {
            RouterTarget target = new RouterTarget(
                (String) map.get("targetId"),
                TargetType.valueOf((String) map.get("type"))
           );
            if(map.containsKey("metadata")) {
                target.metadata.putAll((Map<String, Object>) map.get("metadata"));
            }
            return target;
        }
    }

    /**
     * Routing result
     */
    public static class RoutingResult {
        private final RoutingStatus status;
        private final List<RouterTarget> targets;
        private final String message;

        private RoutingResult(RoutingStatus status, List<RouterTarget> targets, String message) {
            this.status = status;
            this.targets = targets;
            this.message = message;
        }

        public static RoutingResult success(List<RouterTarget> targets) {
            return new RoutingResult(RoutingStatus.SUCCESS, targets, null);
        }

        public static RoutingResult noMatch() {
            return new RoutingResult(RoutingStatus.NO_MATCH, null, "No matching route");
        }

        public static RoutingResult filtered() {
            return new RoutingResult(RoutingStatus.FILTERED, null, "Message filtered");
        }

        public static RoutingResult hold() {
            return new RoutingResult(RoutingStatus.HOLD, null, "Message held for aggregation");
        }

        public static RoutingResult inactive() {
            return new RoutingResult(RoutingStatus.INACTIVE, null, "Router is inactive");
        }

        public static RoutingResult error(String message) {
            return new RoutingResult(RoutingStatus.ERROR, null, message);
        }

        public RoutingStatus getStatus() { return status; }
        public List<RouterTarget> getTargets() { return targets; }
        public String getMessage() { return message; }
    }

    // Enums

    public enum TargetType {
        FLOW,      // Another flow
        ADAPTER,   // Direct to adapter
        PROCESSOR, // Processing step
        ROUTER      // Another router
    }

    public enum RoutingStatus {
        SUCCESS,
        NO_MATCH,
        FILTERED,
        HOLD,
        INACTIVE,
        ERROR
    }

    public enum SplitStrategy {
        XPATH,
        JSONPATH,
        DELIMITER,
        LINE
    }

    public enum AggregationStrategy {
        CONCAT,
        LIST,
        XML_COMBINE,
        JSON_ARRAY,
        CUSTOM
    }
}
