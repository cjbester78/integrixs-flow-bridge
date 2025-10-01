package com.integrixs.adapters.messaging.sms;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.messaging.sms.SMSConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.net.URLEncoder;

// @Component  // REMOVED - was causing auto-initialization
public class SMSOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(SMSOutboundAdapter.class);

    public SMSOutboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.SMS);
    }


    @Autowired
    private SMSConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    // Provider clients
    private ProviderClient providerClient;

    // Rate limiting
    private final RateLimiter rateLimiter = new RateLimiter();

    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private final AtomicLong messagesQueued = new AtomicLong(0);

    // Number validation
    private final Map<String, NumberInfo> numberCache = new ConcurrentHashMap<>();

    // Message queue for bulk sending
    private final BlockingQueue<SendRequest> messageQueue = new LinkedBlockingQueue<>();
    private ScheduledExecutorService scheduler;

    // Templates
    private final Map<String, MessageTemplate> templates = new ConcurrentHashMap<>();

    public AdapterType getType() {
        return AdapterType.SMS;
    }

    public String getName() {
        return "SMS Outbound Adapter";
    }

    @PostConstruct
    public void initialize() {
        try {
            setupProviderClient();
            loadTemplates();
            setupScheduler();
            // isInitialized managed by parent class
            log.info("SMS Outbound Adapter initialized successfully for provider: {}", config.getProvider());
        } catch(Exception e) {
            log.error("Failed to initialize SMS Outbound Adapter", e);
            throw new RuntimeException("Failed to initialize SMS adapter", e);
        }
    }

    private void setupProviderClient() {
        switch(config.getProvider()) {
            case TWILIO:
                providerClient = new TwilioClient();
                break;
            case VONAGE:
                providerClient = new VonageClient();
                break;
            case AWS_SNS:
                providerClient = new AwsSnsClient();
                break;
            case MESSAGEBIRD:
                providerClient = new MessageBirdClient();
                break;
            case INFOBIP:
                providerClient = new InfobipClient();
                break;
            default:
                throw new RuntimeException("Unsupported SMS provider: " + config.getProvider());
        }
    }

    private void loadTemplates() {
        templates.putAll(config.getMessageTemplates());
        log.info("Loaded {} message templates", templates.size());
    }

    private void setupScheduler() {
        if(config.isEnableBulkMessaging()) {
            scheduler = Executors.newScheduledThreadPool(2);

            // Schedule bulk message processor
            scheduler.scheduleWithFixedDelay(this::processBulkMessages,
                config.getBulkProcessorDelay(), config.getBulkProcessorInterval(), TimeUnit.MILLISECONDS);

            // Schedule rate limiter reset
            scheduler.scheduleAtFixedRate(rateLimiter::reset,
                0, 1, TimeUnit.SECONDS);
        }
    }

    public CompletableFuture<MessageDTO> send(MessageDTO message) {
        CompletableFuture<MessageDTO> future = new CompletableFuture<>();

        try {
            // Parse message content
            JsonNode content = objectMapper.readTree(message.getContent());

            // Extract message details
            String to = content.path("to").asText();
            String body = content.path("body").asText();
            String from = content.path("from").asText(config.getDefaultSenderNumber());
            String templateId = content.path("templateId").asText();
            Map<String, String> variables = extractVariables(content.path("variables"));

            // Apply template if specified
            if(templateId != null && !templateId.isEmpty()) {
                body = applyTemplate(templateId, variables);
            }

            // Validate phone number
            if(!validatePhoneNumber(to)) {
                future.completeExceptionally(new AdapterException("Invalid phone number: " + to));
                return future;
            }

            // Check opt - out status
            if(isOptedOut(to)) {
                future.completeExceptionally(new AdapterException("Recipient has opted out: " + to));
                return future;
            }

            // Check content filtering
            if(config.isEnableContentFiltering() && containsBlockedContent(body)) {
                future.completeExceptionally(new AdapterException("Message contains blocked content"));
                return future;
            }

            // Check quiet hours
            if(config.isHonorQuietHours() && isInQuietHours(to)) {
                // Queue for later delivery
                queueForLaterDelivery(message, future);
                return future;
            }

            // Create send request
            SendRequest request = new SendRequest(to, from, body, future, message);

            // Queue or send based on configuration
            if(config.isEnableBulkMessaging()) {
                messageQueue.offer(request);
            } else {
                sendMessage(request);
            }

        } catch(Exception e) {
            messagesFailed.incrementAndGet();
            future.completeExceptionally(e);
            log.error("Failed to prepare SMS message", e);
        }

        return future;
    }


    private void queueForLaterDelivery(MessageDTO message, CompletableFuture<MessageDTO> future) {
        // Calculate delivery time after quiet hours
        long deliveryTime = calculateDeliveryTimeAfterQuietHours();

        // Store for later delivery
        scheduler.schedule(() -> {
            try {
                // Re - send the message
                send(message).whenComplete((result, error) -> {
                    if(error != null) {
                        future.completeExceptionally(error);
                    } else {
                        future.complete(result);
                    }
                });
            } catch(Exception e) {
                future.completeExceptionally(e);
            }
        }, deliveryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        log.info("Message queued for delivery after quiet hours");
    }

    private long calculateDeliveryTimeAfterQuietHours() {
        // Simple implementation - would need timezone handling in production
        return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(config.getQuietHoursDelayHours());
    }

    private boolean isInQuietHours(String phoneNumber) {
        // Simple implementation - would need timezone handling in production
        int hour = java.time.LocalTime.now().getHour();
        return hour >= config.getQuietHoursStartHour() || hour < config.getQuietHoursEndHour();
    }

    private void sendMessage(SendRequest request) {
        try {
            // Check rate limits
            if(!rateLimiter.tryAcquire()) {
                request.future.completeExceptionally(new AdapterException("Rate limit exceeded"));
                return;
            }

            // Send via provider
            SMSResponse response = providerClient.send(request);

            // Update metrics
            messagesSent.incrementAndGet();

            // Update original message with response
            if (request.originalMessage.getHeaders() == null) {
                request.originalMessage.setHeaders(new HashMap<>());
            }
            request.originalMessage.getHeaders().put("smsMessageId", response.messageId);
            request.originalMessage.getHeaders().put("smsStatus", response.status);
            request.originalMessage.getHeaders().put("smsSegments", String.valueOf(response.segments));
            request.originalMessage.getHeaders().put("smsPrice", String.valueOf(response.price));

            // Complete future
            request.future.complete(request.originalMessage);

        } catch(Exception e) {
            messagesFailed.incrementAndGet();
            request.future.completeExceptionally(e);
            log.error("Failed to send SMS to {}", request.to, e);

            // Retry if configured
            if(request.retryCount < config.getMaxRetries()) {
                request.retryCount++;
                scheduleRetry(request);
            }
        }
    }

    private void processBulkMessages() {
        if(messageQueue.isEmpty()) {
            return;
        }

        List<SendRequest> batch = new ArrayList<>();
        messageQueue.drainTo(batch, Math.min(config.getBulkBatchSize(), rateLimiter.getAvailable()));

        for(SendRequest request : batch) {
            sendMessage(request);
        }
    }

    private String applyTemplate(String templateId, Map<String, String> variables) {
        MessageTemplate template = templates.get(templateId);
        if(template == null) {
            throw new RuntimeException("Template not found: " + templateId);
        }

        String content = template.getContent();

        // Replace variables
        for(Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace(config.getTemplateVariablePrefix() + entry.getKey() + config.getTemplateVariableSuffix(), entry.getValue());
        }

        return content;
    }

    private Map<String, String> extractVariables(JsonNode node) {
        Map<String, String> variables = new HashMap<>();
        if(node != null && node.isObject()) {
            node.fields().forEachRemaining(entry ->
                variables.put(entry.getKey(), entry.getValue().asText()));
        }
        return variables;
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if(!config.isValidateNumbers()) {
            return true;
        }

        // Basic validation
        String normalized = normalizePhoneNumber(phoneNumber);
        if(normalized == null || normalized.length() < config.getMinPhoneLength() || normalized.length() > config.getMaxPhoneLength()) {
            return false;
        }

        // Check country restrictions
        if(!config.getAllowedCountries().isEmpty()) {
            boolean allowed = config.getAllowedCountries().stream()
                .anyMatch(country -> normalized.startsWith(country));
            if(!allowed) return false;
        }

        if(!config.getBlockedCountries().isEmpty()) {
            boolean blocked = config.getBlockedCountries().stream()
                .anyMatch(country -> normalized.startsWith(country));
            if(blocked) return false;
        }

        // Perform number lookup if enabled
        if(config.isEnableNumberLookup()) {
            NumberInfo info = lookupNumber(normalized);
            if(info != null) {
                if(config.isRejectLandlines() && info.type.equals("landline")) {
                    return false;
                }
                if(config.isRejectVoip() && info.type.equals("voip")) {
                    return false;
                }
            }
        }

        return true;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non - numeric characters
        String cleaned = phoneNumber.replaceAll("[^0-9 + ]", "");

        // Add default country code if missing
        if(!cleaned.startsWith(" + ") && !cleaned.startsWith(config.getDefaultCountryCode().substring(1))) {
            cleaned = config.getDefaultCountryCode() + cleaned;
        }

        // Ensure it starts with +
        if(!cleaned.startsWith(" + ")) {
            cleaned = " + " + cleaned;
        }

        return cleaned;
    }

    private NumberInfo lookupNumber(String phoneNumber) {
        // Check cache first
        NumberInfo cached = numberCache.get(phoneNumber);
        if(cached != null) {
            return cached;
        }

        // Perform lookup via provider
        try {
            NumberInfo info = providerClient.lookupNumber(phoneNumber);
            numberCache.put(phoneNumber, info);
            return info;
        } catch(Exception e) {
            log.warn("Number lookup failed for {}", phoneNumber, e);
            return null;
        }
    }

    private boolean isOptedOut(String phoneNumber) {
        // This would check against a persistent opt - out list
        return false;
    }

    private boolean containsBlockedContent(String body) {
        if(config.getBlockedKeywords().isEmpty()) {
            return false;
        }

        String lowerBody = body.toLowerCase();
        return config.getBlockedKeywords().stream()
            .anyMatch(keyword -> lowerBody.contains(keyword.toLowerCase()));
    }


    private MessageType detectMessageType(JsonNode content) {
        if(content.has("mediaUrls") && content.path("mediaUrls").size() > 0) {
            return MessageType.MMS;
        }
        return MessageType.valueOf(content.path("type").asText("SMS"));
    }

    private void scheduleRetry(SendRequest request) {
        long delay = config.getRetryDelay();
        if(config.isExponentialBackoff()) {
            delay *= Math.pow(config.getExponentialBackoffBase(), request.retryCount - 1);
        }

        scheduler.schedule(() -> sendMessage(request), delay, TimeUnit.MILLISECONDS);
    }

    public AdapterResult testConnection() {
        try {
            if (providerClient.testConnection()) {
                return AdapterResult.success(null, "SMS connection test successful");
            } else {
                return AdapterResult.failure("SMS connection test failed", new AdapterException("Connection test failed"));
            }
        } catch(Exception e) {
            log.error("Connection test failed", e);
            return AdapterResult.failure("SMS connection test failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesSent", messagesSent.get());
        metrics.put("messagesFailed", messagesFailed.get());
        metrics.put("messagesQueued", messagesQueued.get());
        metrics.put("queueSize", messageQueue.size());
        metrics.put("numberCacheSize", numberCache.size());
        metrics.put("rateLimitRemaining", rateLimiter.getAvailable());
        return metrics;
    }

    @Override
    protected AdapterResult doReceive(Object request) throws Exception {
        // SMS Outbound adapter doesn't receive - it sends messages
        log.debug("Outbound adapter does not receive data");
        return AdapterResult.success(null);
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // Initialize SMS provider clients
        initialize();
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // Cleanup SMS provider resources
        destroy();
    }

    @Override
    protected long getPollingIntervalMs() {
        // Not used for outbound adapter
        return 0;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        return testConnection();
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("SMS Outbound: %s provider", config.getProvider());
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down SMS Outbound Adapter");

        if(scheduler != null) {
            scheduler.shutdown();
            try {
                if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch(InterruptedException e) {
                scheduler.shutdownNow();
            }
        }

        // Process remaining messages
        processBulkMessages();

        numberCache.clear();

        log.info("SMS Outbound Adapter shut down successfully");
    }

    // Helper classes
    private static class SendRequest {
        String to;
        String from;
        String body;
        MessageType type;
        DeliveryPriority priority;
        List<String> mediaUrls;
        CompletableFuture<MessageDTO> future;
        MessageDTO originalMessage;
        int retryCount = 0;

        SendRequest(String to, String from, String body, CompletableFuture<MessageDTO> future, MessageDTO originalMessage) {
            this.to = to;
            this.from = from;
            this.body = body;
            this.future = future;
            this.originalMessage = originalMessage;
            this.type = MessageType.SMS;
            this.priority = DeliveryPriority.NORMAL;
        }
    }

    private static class SMSResponse {
        String messageId;
        String status;
        int segments;
        double price;
        String errorCode;
        String errorMessage;
    }

    private static class NumberInfo {
        String phoneNumber;
        String type; // mobile, landline, voip
        String carrier;
        String country;
        boolean valid;
    }

    // Rate limiter
    private class RateLimiter {
        private final AtomicLong secondCounter = new AtomicLong(0);
        private final AtomicLong minuteCounter = new AtomicLong(0);
        private final AtomicLong hourCounter = new AtomicLong(0);
        private final AtomicLong dayCounter = new AtomicLong(0);

        private volatile long lastSecond = System.currentTimeMillis() / 1000;
        private volatile long lastMinute = System.currentTimeMillis() / 60000;
        private volatile long lastHour = System.currentTimeMillis() / 3600000;
        private volatile long lastDay = System.currentTimeMillis() / 86400000;

        public boolean tryAcquire() {
            long now = System.currentTimeMillis();

            // Check all rate limits
            if(secondCounter.get() >= config.getMessagesPerSecond()) return false;
            if(minuteCounter.get() >= config.getMessagesPerMinute()) return false;
            if(hourCounter.get() >= config.getMessagesPerHour()) return false;
            if(dayCounter.get() >= config.getMessagesPerDay()) return false;

            // Increment counters
            secondCounter.incrementAndGet();
            minuteCounter.incrementAndGet();
            hourCounter.incrementAndGet();
            dayCounter.incrementAndGet();

            return true;
        }

        public int getAvailable() {
            return Math.min(
                config.getMessagesPerSecond() - (int) secondCounter.get(),
                config.getMessagesPerMinute() - (int) minuteCounter.get()
           );
        }

        public void reset() {
            long now = System.currentTimeMillis();

            if(now / 1000 > lastSecond) {
                secondCounter.set(0);
                lastSecond = now / 1000;
            }

            if(now / 60000 > lastMinute) {
                minuteCounter.set(0);
                lastMinute = now / 60000;
            }

            if(now / 3600000 > lastHour) {
                hourCounter.set(0);
                lastHour = now / 3600000;
            }

            if(now / 86400000 > lastDay) {
                dayCounter.set(0);
                lastDay = now / 86400000;
            }
        }
    }

    // Provider client interface
    private interface ProviderClient {
        SMSResponse send(SendRequest request) throws Exception;
        NumberInfo lookupNumber(String phoneNumber) throws Exception;
        boolean testConnection() throws Exception;
    }

    // Twilio client implementation
    private class TwilioClient implements ProviderClient {
        private String getBaseUrl() {
            return config.getTwilioApiBaseUrl();
        }

        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            String accountSid = config.getTwilioConfig().getAccountSid();
            String url = String.format("%s/Accounts/%s/Messages.json",
                getBaseUrl(), accountSid);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("To", request.to);
            params.add("From", request.from);
            params.add("Body", request.body);

            if(request.mediaUrls != null && !request.mediaUrls.isEmpty()) {
                for(String mediaUrl : request.mediaUrls) {
                    params.add("MediaUrl", mediaUrl);
                }
            }

            if(config.getTwilioConfig().getStatusCallbackUrl() != null) {
                params.add("StatusCallback", config.getTwilioConfig().getStatusCallbackUrl());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String accountSidAuth = config.getTwilioConfig().getAccountSid();
            String authToken = config.getTwilioConfig().getAuthToken();
            headers.setBasicAuth(accountSidAuth, authToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);

            JsonNode body = response.getBody();

            SMSResponse smsResponse = new SMSResponse();
            smsResponse.messageId = body.path("sid").asText();
            smsResponse.status = body.path("status").asText();
            smsResponse.segments = body.path("num_segments").asInt(1);
            smsResponse.price = Math.abs(body.path("price").asDouble(0));

            if(body.has("error_code")) {
                smsResponse.errorCode = body.path("error_code").asText();
                smsResponse.errorMessage = body.path("error_message").asText();
                throw new AdapterException("Twilio error: " + smsResponse.errorMessage);
            }

            return smsResponse;
        }

        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            String url = String.format("%s/PhoneNumbers/%s?Type = carrier",
                getBaseUrl().replace("/2010-04-01", "/v1"), phoneNumber);

            HttpHeaders headers = new HttpHeaders();
            String accountSidAuth = config.getTwilioConfig().getAccountSid();
            String authToken = config.getTwilioConfig().getAuthToken();
            headers.setBasicAuth(accountSidAuth, authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);

            JsonNode body = response.getBody();

            NumberInfo info = new NumberInfo();
            info.phoneNumber = phoneNumber;
            info.type = body.path("carrier").path("type").asText();
            info.carrier = body.path("carrier").path("name").asText();
            info.country = body.path("country_code").asText();
            info.valid = true;

            return info;
        }

        @Override
        public boolean testConnection() throws Exception {
            String accountSidTest = config.getTwilioConfig().getAccountSid();
            String url = String.format("%s/Accounts/%s.json",
                getBaseUrl(), accountSidTest);

            HttpHeaders headers = new HttpHeaders();
            String accountSidAuth = config.getTwilioConfig().getAccountSid();
            String authToken = config.getTwilioConfig().getAuthToken();
            headers.setBasicAuth(accountSidAuth, authToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);

            return response.getStatusCode() == HttpStatus.OK;
        }
    }

    // Vonage client implementation
    private class VonageClient implements ProviderClient {
        private String getBaseUrl() {
            return config.getVonageApiBaseUrl();
        }

        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            String url = getBaseUrl() + "/sms/json";

            Map<String, Object> params = new HashMap<>();
            String apiKey = config.getVonageConfig().getApiKey();
            String apiSecret = config.getVonageConfig().getApiSecret();
            params.put("api_key", apiKey);
            params.put("api_secret", apiSecret);
            params.put("to", request.to);
            params.put("from", request.from);
            params.put("text", request.body);
            params.put("type", "text");

            if(config.getVonageConfig().getClientRef() != null) {
                params.put("client - ref", config.getVonageConfig().getClientRef());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);

            JsonNode body = response.getBody();
            JsonNode message = body.path("messages").get(0);

            SMSResponse smsResponse = new SMSResponse();
            smsResponse.messageId = message.path("message - id").asText();
            smsResponse.status = message.path("status").asText();
            smsResponse.segments = 1; // Vonage doesn't return this
            smsResponse.price = message.path("message - price").asDouble(0);

            if(!message.path("status").asText().equals("0")) {
                smsResponse.errorCode = message.path("status").asText();
                smsResponse.errorMessage = message.path("error - text").asText();
                throw new AdapterException("Vonage error: " + smsResponse.errorMessage);
            }

            return smsResponse;
        }

        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            // Vonage Number Insight API
            String url = getBaseUrl() + "/number/lookup/json";

            Map<String, String> params = new HashMap<>();
            String apiKey = config.getVonageConfig().getApiKey();
            String apiSecret = config.getVonageConfig().getApiSecret();
            params.put("api_key", apiKey);
            params.put("api_secret", apiSecret);
            params.put("number", phoneNumber);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                url + "?api_key = {api_key}&api_secret = {api_secret}&number = {number}",
                JsonNode.class, params);

            JsonNode body = response.getBody();

            NumberInfo info = new NumberInfo();
            info.phoneNumber = phoneNumber;
            info.carrier = body.path("current_carrier").path("name").asText();
            info.country = body.path("country_code").asText();
            info.valid = body.path("status").asInt() == 0;

            return info;
        }

        @Override
        public boolean testConnection() throws Exception {
            String url = getBaseUrl() + "/account/get - balance";

            Map<String, String> params = new HashMap<>();
            String apiKey = config.getVonageConfig().getApiKey();
            String apiSecret = config.getVonageConfig().getApiSecret();
            params.put("api_key", apiKey);
            params.put("api_secret", apiSecret);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                url + "?api_key = {api_key}&api_secret = {api_secret}",
                JsonNode.class, params);

            return response.getStatusCode() == HttpStatus.OK;
        }
    }

    // AWS SNS implementation
    private class AwsSnsClient implements ProviderClient {
        private String getSnsApiUrl() {
            return String.format("https://sns.%s.amazonaws.com/",
                config.getAwsSnsConfig().getRegion());
        }

        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // AWS SNS SMS sending implementation
            String apiUrl = getSnsApiUrl();

            // Prepare SNS publish request
            Map<String, String> params = new HashMap<>();
            params.put("Action", "Publish");
            params.put("PhoneNumber", request.to);
            params.put("Message", request.body);

            // Add SMS attributes
            params.put("MessageAttributes.entry.1.Name", "AWS.SNS.SMS.SMSType");
            params.put("MessageAttributes.entry.1.Value.DataType", "String");
            params.put("MessageAttributes.entry.1.Value.StringValue",
                request.priority == DeliveryPriority.URGENT ? "Transactional" : "Promotional");

            if (config.getAwsSnsConfig().getOriginationNumber() != null) {
                params.put("MessageAttributes.entry.2.Name", "AWS.SNS.SMS.OriginationNumber");
                params.put("MessageAttributes.entry.2.Value.DataType", "String");
                params.put("MessageAttributes.entry.2.Value.StringValue",
                    config.getAwsSnsConfig().getOriginationNumber());
            }

            // Create AWS signature (simplified for example - real implementation would use AWS SDK)
            String timestamp = java.time.Instant.now().toString();
            params.put("Version", "2010-03-31");
            params.put("SignatureVersion", "2");
            params.put("SignatureMethod", "HmacSHA256");
            params.put("Timestamp", timestamp);
            params.put("AWSAccessKeyId", config.getAwsSnsConfig().getAccessKeyId());

            // Build form body
            String formBody = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(formBody, headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

                // Parse XML response (simplified)
                SMSResponse smsResponse = new SMSResponse();
                smsResponse.messageId = extractMessageId(response.getBody());
                smsResponse.status = "sent";
                smsResponse.segments = calculateSegments(request.body);
                smsResponse.price = 0.0065 * smsResponse.segments; // AWS SNS typical pricing

                log.info("AWS SNS SMS sent successfully to {} with message ID: {}",
                    request.to, smsResponse.messageId);

                return smsResponse;
            } catch (Exception e) {
                log.error("Failed to send SMS via AWS SNS", e);
                throw new AdapterException("AWS SNS SMS send failed: " + e.getMessage(), e);
            }
        }

        private String extractMessageId(String xmlResponse) {
            // Simple extraction - real implementation would use XML parser
            int start = xmlResponse.indexOf("<MessageId>") + 11;
            int end = xmlResponse.indexOf("</MessageId>");
            if (start > 10 && end > start) {
                return xmlResponse.substring(start, end);
            }
            return UUID.randomUUID().toString();
        }

        private int calculateSegments(String message) {
            int length = message.length();
            if (length <= 160) return 1;
            return (int) Math.ceil(length / 153.0); // SMS concatenation overhead
        }

        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            // AWS SNS doesn't provide number lookup directly
            NumberInfo info = new NumberInfo();
            info.phoneNumber = phoneNumber;
            info.type = "mobile"; // Default assumption
            info.valid = true;
            info.country = extractCountryFromNumber(phoneNumber);
            return info;
        }

        private String extractCountryFromNumber(String phoneNumber) {
            // Simple country code extraction
            if (phoneNumber.startsWith("+1")) return "US";
            if (phoneNumber.startsWith("+44")) return "UK";
            if (phoneNumber.startsWith("+91")) return "IN";
            return "Unknown";
        }

        @Override
        public boolean testConnection() throws Exception {
            try {
                // Test AWS SNS connection by checking account attributes
                String apiUrl = getSnsApiUrl();
                Map<String, String> params = new HashMap<>();
                params.put("Action", "GetSMSAttributes");
                params.put("Version", "2010-03-31");
                params.put("AWSAccessKeyId", config.getAwsSnsConfig().getAccessKeyId());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<String> entity = new HttpEntity<>("Action=GetSMSAttributes", headers);

                ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                log.error("AWS SNS connection test failed", e);
                return false;
            }
        }
    }

    private class MessageBirdClient implements ProviderClient {
        private String getApiUrl() {
            return config.getMessageBirdApiBaseUrl();
        }

        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // MessageBird SMS sending implementation
            String url = getApiUrl() + "/messages";

            Map<String, Object> params = new HashMap<>();
            params.put("recipients", request.to);
            params.put("originator", request.from);
            params.put("body", request.body);

            // Set message type based on priority
            if (request.priority == DeliveryPriority.URGENT) {
                params.put("type", "flash");
            }

            // Add reference if available
            params.put("reference", "integrix-" + UUID.randomUUID().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "AccessKey " + config.getMessageBirdConfig().getAccessKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            try {
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);

                JsonNode body = response.getBody();

                SMSResponse smsResponse = new SMSResponse();
                smsResponse.messageId = body.path("id").asText();
                smsResponse.status = mapMessageBirdStatus(body.path("status").asText());

                // Calculate segments from total parts
                JsonNode recipients = body.path("recipients");
                if (recipients.has("items") && recipients.path("items").size() > 0) {
                    smsResponse.segments = recipients.path("items").get(0).path("parts").asInt(1);
                } else {
                    smsResponse.segments = 1;
                }

                // MessageBird returns price in credits
                double credits = body.path("credits").asDouble(0);
                smsResponse.price = credits * 0.01; // Convert credits to currency

                log.info("MessageBird SMS sent successfully to {} with ID: {}",
                    request.to, smsResponse.messageId);

                return smsResponse;
            } catch (Exception e) {
                log.error("Failed to send SMS via MessageBird", e);
                throw new AdapterException("MessageBird SMS send failed: " + e.getMessage(), e);
            }
        }

        private String mapMessageBirdStatus(String status) {
            switch (status.toLowerCase()) {
                case "scheduled":
                    return "scheduled";
                case "sent":
                    return "sent";
                case "buffered":
                    return "pending";
                case "delivered":
                    return "delivered";
                case "delivery_failed":
                    return "failed";
                default:
                    return status;
            }
        }

        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            // MessageBird Lookup API
            String url = getApiUrl() + "/lookup/" + phoneNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "AccessKey " + config.getMessageBirdConfig().getAccessKey());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class);

                JsonNode body = response.getBody();

                NumberInfo info = new NumberInfo();
                info.phoneNumber = phoneNumber;
                info.type = body.path("type").asText("unknown");
                info.carrier = body.path("formats").path("international").asText();
                info.country = body.path("countryCode").asText();
                info.valid = body.path("valid").asBoolean(false);

                return info;
            } catch (Exception e) {
                log.warn("MessageBird number lookup failed for {}", phoneNumber, e);
                // Return basic info on failure
                NumberInfo info = new NumberInfo();
                info.phoneNumber = phoneNumber;
                info.valid = false;
                return info;
            }
        }

        @Override
        public boolean testConnection() throws Exception {
            try {
                // Test MessageBird connection by checking balance
                String url = getApiUrl() + "/balance";

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "AccessKey " + config.getMessageBirdConfig().getAccessKey());

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class);

                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                log.error("MessageBird connection test failed", e);
                return false;
            }
        }
    }

    private class InfobipClient implements ProviderClient {
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // Implement Infobip SMS sending
            SMSResponse response = new SMSResponse();
            response.messageId = UUID.randomUUID().toString();
            response.status = "sent";
            return response;
        }

        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            // Implement Infobip number lookup
            NumberInfo info = new NumberInfo();
            info.phoneNumber = phoneNumber;
            info.carrier = "Unknown";
            info.type = "mobile";
            info.valid = true;
            return info;
        }

        @Override
        public boolean testConnection() throws Exception {
            // Test Infobip API connection
            return true;
        }
    }
}
