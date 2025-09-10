package com.integrixs.adapters.messaging.sms;

import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.messaging.sms.SMSConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

@Component
@Slf4j
public class SMSOutboundAdapter extends AbstractOutboundAdapter {
    
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
    
    @Override
    public AdapterType getType() {
        return AdapterType.SMS;
    }
    
    @Override
    public String getName() {
        return "SMS Outbound Adapter";
    }
    
    @PostConstruct
    public void initialize() {
        try {
            setupProviderClient();
            loadTemplates();
            setupScheduler();
            isInitialized = true;
            log.info("SMS Outbound Adapter initialized successfully for provider: {}", config.getProvider());
        } catch (Exception e) {
            log.error("Failed to initialize SMS Outbound Adapter", e);
            throw new AdapterException("Failed to initialize SMS adapter", e);
        }
    }
    
    private void setupProviderClient() {
        switch (config.getProvider()) {
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
                throw new AdapterException("Unsupported SMS provider: " + config.getProvider());
        }
    }
    
    private void loadTemplates() {
        templates.putAll(config.getMessageTemplates());
        log.info("Loaded {} message templates", templates.size());
    }
    
    private void setupScheduler() {
        if (config.getFeatures().isEnableBulkMessaging()) {
            scheduler = Executors.newScheduledThreadPool(2);
            
            // Schedule bulk message processor
            scheduler.scheduleWithFixedDelay(this::processBulkMessages, 
                1000, 1000, TimeUnit.MILLISECONDS);
                
            // Schedule rate limiter reset
            scheduler.scheduleAtFixedRate(rateLimiter::reset, 
                0, 1, TimeUnit.SECONDS);
        }
    }
    
    @Override
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
            if (templateId != null && !templateId.isEmpty()) {
                body = applyTemplate(templateId, variables);
            }
            
            // Validate phone number
            if (!validatePhoneNumber(to)) {
                future.completeExceptionally(new AdapterException("Invalid phone number: " + to));
                return future;
            }
            
            // Check opt-out status
            if (isOptedOut(to)) {
                future.completeExceptionally(new AdapterException("Recipient has opted out: " + to));
                return future;
            }
            
            // Check content filtering
            if (config.isEnableContentFiltering() && containsBlockedContent(body)) {
                future.completeExceptionally(new AdapterException("Message contains blocked content"));
                return future;
            }
            
            // Check quiet hours
            if (config.getCompliance().isHonorQuietHours() && isInQuietHours(to)) {
                // Queue for later delivery
                queueForLaterDelivery(message, future);
                return future;
            }
            
            // Create send request
            SendRequest request = new SendRequest();
            request.to = normalizePhoneNumber(to);
            request.from = from;
            request.body = body;
            request.type = detectMessageType(content);
            request.priority = config.getPriority();
            request.future = future;
            request.originalMessage = message;
            
            // Add media URLs for MMS
            if (content.has("mediaUrls")) {
                request.mediaUrls = new ArrayList<>();
                content.path("mediaUrls").forEach(url -> request.mediaUrls.add(url.asText()));
            }
            
            // Send immediately or queue for bulk
            if (config.getFeatures().isEnableBulkMessaging()) {
                messageQueue.offer(request);
                messagesQueued.incrementAndGet();
            } else {
                sendMessage(request);
            }
            
        } catch (Exception e) {
            messagesFailed.incrementAndGet();
            future.completeExceptionally(e);
            log.error("Failed to send SMS", e);
        }
        
        return future;
    }
    
    private void sendMessage(SendRequest request) {
        try {
            // Check rate limits
            if (!rateLimiter.tryAcquire()) {
                request.future.completeExceptionally(new AdapterException("Rate limit exceeded"));
                return;
            }
            
            // Send via provider
            SMSResponse response = providerClient.send(request);
            
            // Update metrics
            messagesSent.incrementAndGet();
            
            // Update original message with response
            request.originalMessage.getMetadata().put("smsMessageId", response.messageId);
            request.originalMessage.getMetadata().put("smsStatus", response.status);
            request.originalMessage.getMetadata().put("smsSegments", String.valueOf(response.segments));
            request.originalMessage.getMetadata().put("smsPrice", String.valueOf(response.price));
            
            // Complete future
            request.future.complete(request.originalMessage);
            
        } catch (Exception e) {
            messagesFailed.incrementAndGet();
            request.future.completeExceptionally(e);
            log.error("Failed to send SMS to {}", request.to, e);
            
            // Retry if configured
            if (request.retryCount < config.getMaxRetries()) {
                request.retryCount++;
                scheduleRetry(request);
            }
        }
    }
    
    private void processBulkMessages() {
        if (messageQueue.isEmpty()) {
            return;
        }
        
        List<SendRequest> batch = new ArrayList<>();
        messageQueue.drainTo(batch, Math.min(100, rateLimiter.getAvailable()));
        
        for (SendRequest request : batch) {
            sendMessage(request);
        }
    }
    
    private String applyTemplate(String templateId, Map<String, String> variables) {
        MessageTemplate template = templates.get(templateId);
        if (template == null) {
            throw new AdapterException("Template not found: " + templateId);
        }
        
        String content = template.getContent();
        
        // Replace variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        
        return content;
    }
    
    private Map<String, String> extractVariables(JsonNode node) {
        Map<String, String> variables = new HashMap<>();
        if (node != null && node.isObject()) {
            node.fields().forEachRemaining(entry -> 
                variables.put(entry.getKey(), entry.getValue().asText()));
        }
        return variables;
    }
    
    private boolean validatePhoneNumber(String phoneNumber) {
        if (!config.isValidateNumbers()) {
            return true;
        }
        
        // Basic validation
        String normalized = normalizePhoneNumber(phoneNumber);
        if (normalized == null || normalized.length() < 10 || normalized.length() > 15) {
            return false;
        }
        
        // Check country restrictions
        if (!config.getAllowedCountries().isEmpty()) {
            boolean allowed = config.getAllowedCountries().stream()
                .anyMatch(country -> normalized.startsWith(country));
            if (!allowed) return false;
        }
        
        if (!config.getBlockedCountries().isEmpty()) {
            boolean blocked = config.getBlockedCountries().stream()
                .anyMatch(country -> normalized.startsWith(country));
            if (blocked) return false;
        }
        
        // Perform number lookup if enabled
        if (config.isEnableNumberLookup()) {
            NumberInfo info = lookupNumber(normalized);
            if (info != null) {
                if (config.isRejectLandlines() && info.type.equals("landline")) {
                    return false;
                }
                if (config.isRejectVoip() && info.type.equals("voip")) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-numeric characters
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Add default country code if missing
        if (!cleaned.startsWith("+") && !cleaned.startsWith(config.getDefaultCountryCode().substring(1))) {
            cleaned = config.getDefaultCountryCode() + cleaned;
        }
        
        // Ensure it starts with +
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        
        return cleaned;
    }
    
    private NumberInfo lookupNumber(String phoneNumber) {
        // Check cache first
        NumberInfo cached = numberCache.get(phoneNumber);
        if (cached != null) {
            return cached;
        }
        
        // Perform lookup via provider
        try {
            NumberInfo info = providerClient.lookupNumber(phoneNumber);
            numberCache.put(phoneNumber, info);
            return info;
        } catch (Exception e) {
            log.warn("Number lookup failed for {}", phoneNumber, e);
            return null;
        }
    }
    
    private boolean isOptedOut(String phoneNumber) {
        // This would check against a persistent opt-out list
        return false;
    }
    
    private boolean containsBlockedContent(String body) {
        if (config.getBlockedKeywords().isEmpty()) {
            return false;
        }
        
        String lowerBody = body.toLowerCase();
        return config.getBlockedKeywords().stream()
            .anyMatch(keyword -> lowerBody.contains(keyword.toLowerCase()));
    }
    
    private boolean isInQuietHours(String phoneNumber) {
        // Determine recipient timezone
        // This is simplified - would need proper timezone detection
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        String start = config.getCompliance().getQuietHoursStart();
        String end = config.getCompliance().getQuietHoursEnd();
        
        int startHour = Integer.parseInt(start.split(":")[0]);
        int endHour = Integer.parseInt(end.split(":")[0]);
        
        if (startHour > endHour) {
            // Quiet hours span midnight
            return hour >= startHour || hour < endHour;
        } else {
            return hour >= startHour && hour < endHour;
        }
    }
    
    private void queueForLaterDelivery(MessageDTO message, CompletableFuture<MessageDTO> future) {
        // Calculate next delivery time
        LocalDateTime now = LocalDateTime.now();
        String end = config.getCompliance().getQuietHoursEnd();
        int endHour = Integer.parseInt(end.split(":")[0]);
        
        LocalDateTime deliveryTime = now.withHour(endHour).withMinute(0);
        if (deliveryTime.isBefore(now)) {
            deliveryTime = deliveryTime.plusDays(1);
        }
        
        long delay = deliveryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() 
            - System.currentTimeMillis();
        
        scheduler.schedule(() -> send(message).thenAccept(future::complete)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            }), delay, TimeUnit.MILLISECONDS);
        
        log.info("Message queued for delivery at {}", deliveryTime);
    }
    
    private MessageType detectMessageType(JsonNode content) {
        if (content.has("mediaUrls") && content.path("mediaUrls").size() > 0) {
            return MessageType.MMS;
        }
        return MessageType.valueOf(content.path("type").asText("SMS"));
    }
    
    private void scheduleRetry(SendRequest request) {
        long delay = config.getRetryDelay();
        if (config.isExponentialBackoff()) {
            delay *= Math.pow(2, request.retryCount - 1);
        }
        
        scheduler.schedule(() -> sendMessage(request), delay, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean testConnection() {
        try {
            return providerClient.testConnection();
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = super.getMetrics();
        metrics.put("messagesSent", messagesSent.get());
        metrics.put("messagesFailed", messagesFailed.get());
        metrics.put("messagesQueued", messagesQueued.get());
        metrics.put("queueSize", messageQueue.size());
        metrics.put("numberCacheSize", numberCache.size());
        metrics.put("rateLimitRemaining", rateLimiter.getAvailable());
        return metrics;
    }
    
    @PreDestroy
    public void destroy() {
        log.info("Shutting down SMS Outbound Adapter");
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
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
            if (secondCounter.get() >= config.getMessagesPerSecond()) return false;
            if (minuteCounter.get() >= config.getMessagesPerMinute()) return false;
            if (hourCounter.get() >= config.getMessagesPerHour()) return false;
            if (dayCounter.get() >= config.getMessagesPerDay()) return false;
            
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
            
            if (now / 1000 > lastSecond) {
                secondCounter.set(0);
                lastSecond = now / 1000;
            }
            
            if (now / 60000 > lastMinute) {
                minuteCounter.set(0);
                lastMinute = now / 60000;
            }
            
            if (now / 3600000 > lastHour) {
                hourCounter.set(0);
                lastHour = now / 3600000;
            }
            
            if (now / 86400000 > lastDay) {
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
        private final String baseUrl = "https://api.twilio.com/2010-04-01";
        
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            String url = String.format("%s/Accounts/%s/Messages.json", 
                baseUrl, config.getTwilioConfig().getAccountSid());
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("To", request.to);
            params.add("From", request.from);
            params.add("Body", request.body);
            
            if (request.mediaUrls != null && !request.mediaUrls.isEmpty()) {
                for (String mediaUrl : request.mediaUrls) {
                    params.add("MediaUrl", mediaUrl);
                }
            }
            
            if (config.getTwilioConfig().getStatusCallbackUrl() != null) {
                params.add("StatusCallback", config.getTwilioConfig().getStatusCallbackUrl());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(config.getTwilioConfig().getAccountSid(), 
                config.getTwilioConfig().getAuthToken());
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);
            
            JsonNode body = response.getBody();
            
            SMSResponse smsResponse = new SMSResponse();
            smsResponse.messageId = body.path("sid").asText();
            smsResponse.status = body.path("status").asText();
            smsResponse.segments = body.path("num_segments").asInt(1);
            smsResponse.price = Math.abs(body.path("price").asDouble(0));
            
            if (body.has("error_code")) {
                smsResponse.errorCode = body.path("error_code").asText();
                smsResponse.errorMessage = body.path("error_message").asText();
                throw new AdapterException("Twilio error: " + smsResponse.errorMessage);
            }
            
            return smsResponse;
        }
        
        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            String url = String.format("%s/PhoneNumbers/%s?Type=carrier", 
                baseUrl.replace("/2010-04-01", "/v1"), phoneNumber);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(config.getTwilioConfig().getAccountSid(), 
                config.getTwilioConfig().getAuthToken());
            
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
            String url = String.format("%s/Accounts/%s.json", 
                baseUrl, config.getTwilioConfig().getAccountSid());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(config.getTwilioConfig().getAccountSid(), 
                config.getTwilioConfig().getAuthToken());
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        }
    }
    
    // Vonage client implementation
    private class VonageClient implements ProviderClient {
        private final String baseUrl = "https://rest.nexmo.com";
        
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            String url = baseUrl + "/sms/json";
            
            Map<String, Object> params = new HashMap<>();
            params.put("api_key", config.getVonageConfig().getApiKey());
            params.put("api_secret", config.getVonageConfig().getApiSecret());
            params.put("to", request.to);
            params.put("from", request.from);
            params.put("text", request.body);
            params.put("type", "text");
            
            if (config.getVonageConfig().getClientRef() != null) {
                params.put("client-ref", config.getVonageConfig().getClientRef());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);
            
            JsonNode body = response.getBody();
            JsonNode message = body.path("messages").get(0);
            
            SMSResponse smsResponse = new SMSResponse();
            smsResponse.messageId = message.path("message-id").asText();
            smsResponse.status = message.path("status").asText();
            smsResponse.segments = 1; // Vonage doesn't return this
            smsResponse.price = message.path("message-price").asDouble(0);
            
            if (!message.path("status").asText().equals("0")) {
                smsResponse.errorCode = message.path("status").asText();
                smsResponse.errorMessage = message.path("error-text").asText();
                throw new AdapterException("Vonage error: " + smsResponse.errorMessage);
            }
            
            return smsResponse;
        }
        
        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            // Vonage Number Insight API
            String url = baseUrl + "/number/lookup/json";
            
            Map<String, String> params = new HashMap<>();
            params.put("api_key", config.getVonageConfig().getApiKey());
            params.put("api_secret", config.getVonageConfig().getApiSecret());
            params.put("number", phoneNumber);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                url + "?api_key={api_key}&api_secret={api_secret}&number={number}", 
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
            String url = baseUrl + "/account/get-balance";
            
            Map<String, String> params = new HashMap<>();
            params.put("api_key", config.getVonageConfig().getApiKey());
            params.put("api_secret", config.getVonageConfig().getApiSecret());
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                url + "?api_key={api_key}&api_secret={api_secret}", 
                JsonNode.class, params);
            
            return response.getStatusCode() == HttpStatus.OK;
        }
    }
    
    // Placeholder implementations for other providers
    private class AwsSnsClient implements ProviderClient {
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // AWS SNS implementation
            throw new UnsupportedOperationException("AWS SNS provider not yet implemented");
        }
        
        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            return null;
        }
        
        @Override
        public boolean testConnection() throws Exception {
            return true;
        }
    }
    
    private class MessageBirdClient implements ProviderClient {
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // MessageBird implementation
            throw new UnsupportedOperationException("MessageBird provider not yet implemented");
        }
        
        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            return null;
        }
        
        @Override
        public boolean testConnection() throws Exception {
            return true;
        }
    }
    
    private class InfobipClient implements ProviderClient {
        @Override
        public SMSResponse send(SendRequest request) throws Exception {
            // Infobip implementation
            throw new UnsupportedOperationException("Infobip provider not yet implemented");
        }
        
        @Override
        public NumberInfo lookupNumber(String phoneNumber) throws Exception {
            return null;
        }
        
        @Override
        public boolean testConnection() throws Exception {
            return true;
        }
    }
}