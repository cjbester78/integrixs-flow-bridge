package com.integrixs.testing.core;

import com.integrixs.testing.mocks.MockAdapterRegistry;
import com.integrixs.testing.runners.EmbeddedServers;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * JUnit 5 extension for Integrixs flow testing
 */
public class IntegrixsTestExtension implements 
    BeforeAllCallback, AfterAllCallback, 
    BeforeEachCallback, AfterEachCallback,
    ParameterResolver, TestInstancePostProcessor {
    
    private static final String FLOW_CONTEXT_KEY = "integrixs.flow.context";
    private static final String EMBEDDED_SERVERS_KEY = "integrixs.embedded.servers";
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Optional<FlowTest> annotation = AnnotationUtils.findAnnotation(
            context.getRequiredTestClass(), FlowTest.class
       );
        
        if (annotation.isPresent()) {
            FlowTest flowTest = annotation.get();
            
            // Start embedded servers if needed
            if (flowTest.useEmbeddedServers()) {
                EmbeddedServers servers = EmbeddedServers.builder()
                    .withPostgreSQL()
                    .withRabbitMQ()
                    .withRedis()
                    .build();
                
                servers.start();
                context.getStore(ExtensionContext.Namespace.GLOBAL)
                    .put(EMBEDDED_SERVERS_KEY, servers);
            }
            
            // Initialize flow context
            FlowTestContext flowContext = new FlowTestContext();
            flowContext.setFlowDefinition(flowTest.flow());
            flowContext.setEnvironment(flowTest.environment());
            flowContext.setUseMockAdapters(flowTest.useMockAdapters());
            flowContext.setTestDataDir(flowTest.testDataDir());
            flowContext.setTimeout(flowTest.timeout());
            
            // Load flow definition
            if (!flowTest.flow().isEmpty()) {
                flowContext.loadFlow();
            }
            
            // Initialize mock adapters
            if (flowTest.useMockAdapters()) {
                MockAdapterRegistry.getInstance().registerDefaultMocks();
            }
            
            context.getStore(ExtensionContext.Namespace.GLOBAL)
                .put(FLOW_CONTEXT_KEY, flowContext);
        }
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Stop embedded servers
        EmbeddedServers servers = context.getStore(ExtensionContext.Namespace.GLOBAL)
            .get(EMBEDDED_SERVERS_KEY, EmbeddedServers.class);
        if (servers != null) {
            servers.stop();
        }
        
        // Clean up mock adapters
        MockAdapterRegistry.getInstance().reset();
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        FlowTestContext flowContext = getFlowContext(context);
        if (flowContext != null) {
            // Reset flow state before each test
            flowContext.reset();
            
            // Check for @TestData annotation
            Method testMethod = context.getRequiredTestMethod();
            TestData testData = testMethod.getAnnotation(TestData.class);
            if (testData != null) {
                flowContext.loadTestData(testData.value());
            }
        }
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        FlowTestContext flowContext = getFlowContext(context);
        if (flowContext != null) {
            // Collect metrics if enabled
            FlowTest annotation = context.getRequiredTestClass()
                .getAnnotation(FlowTest.class);
            if (annotation != null && annotation.collectMetrics()) {
                flowContext.saveMetrics(context.getDisplayName());
            }
            
            // Clean up resources
            flowContext.cleanup();
        }
    }
    
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        // Inject dependencies
        injectFlowContext(testInstance, context);
        injectMockAdapters(testInstance);
        injectTestUtilities(testInstance);
    }
    
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == FlowTestContext.class ||
               type == FlowExecutor.class ||
               type == FlowAssertions.class ||
               type == MockAdapterBuilder.class;
    }
    
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        FlowTestContext context = getFlowContext(extensionContext);
        
        if (type == FlowTestContext.class) {
            return context;
        } else if (type == FlowExecutor.class) {
            return new FlowExecutor(context);
        } else if (type == FlowAssertions.class) {
            return new FlowAssertions();
        } else if (type == MockAdapterBuilder.class) {
            return new MockAdapterBuilder();
        }
        
        throw new ParameterResolutionException("Cannot resolve parameter of type: " + type);
    }
    
    private FlowTestContext getFlowContext(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.GLOBAL)
            .get(FLOW_CONTEXT_KEY, FlowTestContext.class);
    }
    
    private void injectFlowContext(Object testInstance, ExtensionContext context) throws Exception {
        FlowTestContext flowContext = getFlowContext(context);
        if (flowContext != null) {
            for (Field field : testInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectFlowContext.class) && 
                    field.getType() == FlowTestContext.class) {
                    field.setAccessible(true);
                    field.set(testInstance, flowContext);
                }
            }
        }
    }
    
    private void injectMockAdapters(Object testInstance) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MockAdapter.class)) {
                MockAdapter annotation = field.getAnnotation(MockAdapter.class);
                Object mock = MockAdapterRegistry.getInstance()
                    .getMockAdapter(annotation.value());
                field.setAccessible(true);
                field.set(testInstance, mock);
            }
        }
    }
    
    private void injectTestUtilities(Object testInstance) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(TestUtility.class)) {
                field.setAccessible(true);
                
                if (field.getType() == FlowExecutor.class) {
                    field.set(testInstance, new FlowExecutor(getFlowContext(null)));
                } else if (field.getType() == FlowAssertions.class) {
                    field.set(testInstance, new FlowAssertions());
                }
            }
        }
    }
}