package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import java.util.Map;
import com.integrixs.adapters.config.MailInboundAdapterConfig;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * Mail Sender Adapter implementation for email retrieval and processing(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Supports IMAP/POP3 protocols, email filtering, attachment handling, and S/MIME security.
 */
public class MailInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(MailInboundAdapter.class);


    private final MailInboundAdapterConfig config;
    private final Map<String, String> processedMessages = new ConcurrentHashMap<>();
    private Store mailStore;
    private Folder mailFolder;

    // Polling mechanism fields
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private ScheduledExecutorService pollingExecutor;
    private DataReceivedCallback dataCallback;
    public MailInboundAdapter(MailInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing Mail inbound adapter(inbound) with server: {}: {} using {}",
                config.getMailServerHost(), config.getMailServerPort(), config.getMailProtocol());

        try {
            validateConfiguration();
            // For per - poll mode, we don't maintain persistent connection
            if("permanently".equals(config.getConnectionMode())) {
                connectToMailServer();
            }
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("Mail inbound adapter initialized successfully");
        return AdapterOperationResult.success("Mail inbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying Mail inbound adapter");

        // Stop polling if active
        stopPolling();

        disconnectFromMailServer();
        processedMessages.clear();
        return AdapterOperationResult.success("Mail inbound adapter destroyed");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: Basic mail server connectivity
        testResults.add(
            performMailConnectionTest()
       );
        // Test 2: Folder access
        testResults.add(
            performFolderAccessTest()
       );
        // Test 3: Message filtering test
        testResults.add(
            performMessageFilteringTest()
       );
        return AdapterOperationResult.success(testResults);
    }


    private AdapterOperationResult pollForEmails() throws Exception {
        List<Map<String, Object>> processedEmails = new ArrayList<>();
        Store store = null;
        Folder folder = null;
        try {
            // Get or create connection
            if("permanently".equals(config.getConnectionMode())) {
                store = mailStore;
                folder = mailFolder;
                if(store == null || !store.isConnected() || folder == null || !folder.isOpen()) {
                    connectToMailServer();
                    store = mailStore;
                    folder = mailFolder;
                }
            } else {
                store = createMailStore();
                store.connect();
                folder = store.getFolder(config.getFolderName());
                folder.open(Folder.READ_WRITE); // Need write access for marking as read/moving
            }

            // Build search criteria
            SearchTerm searchTerm = buildSearchCriteria();
            // Get messages
            Message[] messages = searchTerm != null ?
                    folder.search(searchTerm) : folder.getMessages();
            // Apply max messages limit
            int maxMessages = config.getMaxMessages() != null ?
                    Integer.parseInt(config.getMaxMessages()) : messages.length;
            int messagesToProcess = Math.min(messages.length, maxMessages);
            // Process messages
            for(int i = 0; i < messagesToProcess; i++) {
                Message message = messages[i];
                try {
                    if(shouldProcessMessage(message)) {
                        Map<String, Object> emailData = processMessage(message);
                        if(emailData != null) {
                            processedEmails.add(emailData);
                            handlePostProcessing(folder, message);

                            // Mark as processed
                            processedMessages.put(getMessageId(message), String.valueOf(System.currentTimeMillis()));
                        }
                    }
                } catch(Exception e) {
                    log.error("Error processing email message: {}", getMessageId(message), e);

                    if(!config.isContinueOnError()) {
                        throw new AdapterException("Email processing failed for message " + getMessageId(message) + ": " + e.getMessage(), e);
                    }
                }
            }
        } finally {
            if("per - poll".equals(config.getConnectionMode())) {
                if(folder != null && folder.isOpen()) {
                    folder.close(false);
                }
                if(store != null && store.isConnected()) {
                    store.close();
                }
            }
        }
        log.info("Mail inbound adapter polled {} emails from server", processedEmails.size());
        return AdapterOperationResult.success(processedEmails,
                String.format("Retrieved %d emails from mail server", processedEmails.size()));
    }
    private Map<String, Object> processMessage(Message message) throws Exception {
        Map<String, Object> emailData = new HashMap<>();
        // Basic message properties
        emailData.put("messageId", getMessageId(message));
        emailData.put("subject", message.getSubject());
        emailData.put("from", message.getFrom() != null && message.getFrom().length > 0 ?
                message.getFrom()[0].toString() : "");
        emailData.put("to", Arrays.toString(message.getAllRecipients()));
        emailData.put("sentDate", message.getSentDate());
        emailData.put("receivedDate", message.getReceivedDate());
        emailData.put("size", message.getSize());
        // Handle message content
        Object content = message.getContent();
        Map<String, String> contentData = extractContent(content);
        emailData.putAll(contentData);
        // Handle attachments
        if(config.isIncludeAttachments()) {
            List<Map<String, Object>> attachments = extractAttachments(message);
            emailData.put("attachments", attachments);
        }
        // Additional headers if configured
        if(config.isIncludeHeaders()) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<Header> headerEnum = message.getAllHeaders();
            while(headerEnum.hasMoreElements()) {
                Header header = headerEnum.nextElement();
                headers.put(header.getName(), header.getValue());
            }
            emailData.put("headers", headers);
        }
        return emailData;
    }

    private Map<String, String> extractContent(Object content) throws Exception {
        Map<String, String> contentData = new HashMap<>();
        if(content instanceof String) {
            // Plain text content
            contentData.put("textContent", (String) content);
            contentData.put("contentType", "text/plain");
        } else if(content instanceof MimeMultipart) {
            // Multipart message
            MimeMultipart multipart = (MimeMultipart) content;
            StringBuilder textContent = new StringBuilder();
            StringBuilder htmlContent = new StringBuilder();
            for(int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if(bodyPart.isMimeType("text/plain")) {
                    textContent.append(bodyPart.getContent().toString());
                } else if(bodyPart.isMimeType("text/html")) {
                    htmlContent.append(bodyPart.getContent().toString());
                }
            }
            // Include content based on configuration
            switch(config.getContentHandling().toLowerCase()) {
                case "text":
                    contentData.put("textContent", textContent.toString());
                    contentData.put("contentType", "text/plain");
                    break;
                case "html":
                    contentData.put("htmlContent", htmlContent.toString());
                    contentData.put("contentType", "text/html");
                    break;
                case "both":
                default:
                    contentData.put("textContent", textContent.toString());
                    contentData.put("htmlContent", htmlContent.toString());
                    contentData.put("contentType", "multipart");
                    break;
            }
        }
        return contentData;
    }
    private List<Map<String, Object>> extractAttachments(Message message) throws Exception {
        List<Map<String, Object>> attachments = new ArrayList<>();
        Object content = message.getContent();
        if(content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for(int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                    (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {
                    Map<String, Object> attachment = new HashMap<>();
                    attachment.put("fileName", bodyPart.getFileName());
                    attachment.put("contentType", bodyPart.getContentType());
                    attachment.put("size", bodyPart.getSize());
                    // Save attachment to directory if configured
                    if(config.getAttachmentDirectory() != null) {
                        String savedPath = saveAttachment(bodyPart);
                        attachment.put("savedPath", savedPath);
                    } else {
                        // Include content directly
                        try(InputStream is = bodyPart.getInputStream();
                             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while((bytesRead = is.read(buffer)) != -1) {
                                baos.write(buffer, 0, bytesRead);
                            }
                            attachment.put("content", baos.toByteArray());
                        }
                    }
                    attachments.add(attachment);
                }
            }
        }
        return attachments;
    }
    private String saveAttachment(BodyPart bodyPart) throws Exception {
        String fileName = bodyPart.getFileName();
        if(fileName == null) {
            fileName = "attachment_" + System.currentTimeMillis();
        }
        File attachmentDir = new File(config.getAttachmentDirectory());
        if(!attachmentDir.exists()) {
            attachmentDir.mkdirs();
        }
        File attachmentFile = new File(attachmentDir, fileName);
        try(InputStream is = bodyPart.getInputStream();
             FileOutputStream fos = new FileOutputStream(attachmentFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return attachmentFile.getAbsolutePath();
    }

    private void handlePostProcessing(Folder folder, Message message) throws Exception {
        // Mark as read if configured
        if(config.isMarkAsRead()) {
            message.setFlag(Flags.Flag.SEEN, true);
        }
        // Move to processed folder if configured
        if(config.getProcessedFolder() != null) {
            Folder processedFolder = folder.getStore().getFolder(config.getProcessedFolder());
            if(!processedFolder.exists()) {
                processedFolder.create(Folder.HOLDS_MESSAGES);
            }
            folder.copyMessages(new Message[] {message}, processedFolder);
        }
        // Delete after processing if configured
        if(config.isDeleteAfterFetch()) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
    }

    private boolean shouldProcessMessage(Message message) throws Exception {
        String messageId = getMessageId(message);
        // Check if already processed
        if(processedMessages.containsKey(messageId)) {
            return false;
        }
        // Additional filtering can be added here
        return true;
    }

    private String getMessageId(Message message) throws Exception {
        String[] messageIds = message.getHeader("Message - ID");
        if(messageIds != null && messageIds.length > 0) {
            return messageIds[0];
        }
        // Fallback to subject + sent date
        return message.getSubject() + "_" +
               (message.getSentDate() != null ? message.getSentDate().getTime() : System.currentTimeMillis());
    }
    private SearchTerm buildSearchCriteria() throws Exception {
        List<SearchTerm> terms = new ArrayList<>();
        // Unread messages only
        if(config.isFetchUnreadOnly()) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        }
        // Date filters
        if(config.getDateFromFilter() != null) {
            Date fromDate = new SimpleDateFormat("yyyy - MM - dd").parse(config.getDateFromFilter());
            terms.add(new ReceivedDateTerm(ComparisonTerm.GE, fromDate));
        }
        if(config.getDateToFilter() != null) {
            Date toDate = new SimpleDateFormat("yyyy - MM - dd").parse(config.getDateToFilter());
            terms.add(new ReceivedDateTerm(ComparisonTerm.LE, toDate));
        }
        // Subject filter
        if(config.getSubjectFilter() != null && !config.getSubjectFilter().trim().isEmpty()) {
            terms.add(new SubjectTerm(config.getSubjectFilter()));
        }
        // From address filter
        if(config.getFromAddressFilter() != null && !config.getFromAddressFilter().trim().isEmpty()) {
            terms.add(new FromStringTerm(config.getFromAddressFilter()));
        }
        // Today's messages only
        if(config.isFetchFromToday()) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            terms.add(new ReceivedDateTerm(ComparisonTerm.GE, today.getTime()));
        }
        // Combine all terms with AND
        if(terms.isEmpty()) {
            return null;
        } else if(terms.size() == 1) {
            return terms.get(0);
        } else {
            SearchTerm result = terms.get(0);
            for(int i = 1; i < terms.size(); i++) {
                result = new AndTerm(result, terms.get(i));
            }
            return result;
        }
    }
    private void connectToMailServer() throws Exception {
        if(mailStore != null) {
            disconnectFromMailServer();
        }
        mailStore = createMailStore();
        mailStore.connect();
        mailFolder = mailStore.getFolder(config.getFolderName());
        mailFolder.open(Folder.READ_WRITE);
    }

    private Store createMailStore() throws Exception {
        Properties props = new Properties();
        String protocol = config.getMailProtocol().toLowerCase();
        // Configure protocol - specific properties
        if("imap".equals(protocol)) {
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.host", config.getMailServerHost());
            props.setProperty("mail.imap.port", config.getMailServerPort());
            if(config.isUseSSLTLS()) {
                props.setProperty("mail.imap.ssl.enable", "true");
                props.setProperty("mail.imap.ssl.trust", "*");
            }
            props.setProperty("mail.imap.connectiontimeout", config.getConnectionTimeout());
            props.setProperty("mail.imap.timeout", config.getReadTimeout());
        } else if("pop3".equals(protocol)) {
            props.setProperty("mail.store.protocol", "pop3");
            props.setProperty("mail.pop3.host", config.getMailServerHost());
            props.setProperty("mail.pop3.port", config.getMailServerPort());
            if(config.isUseSSLTLS()) {
                props.setProperty("mail.pop3.ssl.enable", "true");
                props.setProperty("mail.pop3.ssl.trust", "*");
            }
            props.setProperty("mail.pop3.connectiontimeout", config.getConnectionTimeout());
            props.setProperty("mail.pop3.timeout", config.getReadTimeout());
        }
        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol);
        return store;
    }

    private void disconnectFromMailServer() {
        if(mailFolder != null && mailFolder.isOpen()) {
            try {
                mailFolder.close(false);
            } catch(Exception e) {
                log.warn("Error closing mail folder", e);
            }
            mailFolder = null;
        }
        if(mailStore != null && mailStore.isConnected()) {
            try {
                mailStore.close();
            } catch(Exception e) {
                log.warn("Error closing mail store", e);
            }
            mailStore = null;
        }
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getMailServerHost() == null || config.getMailServerHost().trim().isEmpty()) {
            throw new AdapterException("Mail server host is required", null);
        }
        if(config.getMailPassword() == null) {
            throw new AdapterException("Mail password is required", null);
        }
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("Mail Sender(Inbound): %s:%s, User: %s, Protocol: %s, Folder: %s, Polling: %sms",
                config.getMailServerHost(),
                config.getMailServerPort(),
                config.getMailUsername(),
                config.getMailProtocol(),
                config.getFolderName(),
                config.getPollingInterval());
    }
    // InboundAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return pollForEmails();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }
    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for this adapter type
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
        // Not implemented for this adapter type
    }

    @Override
    public boolean isListening() {
        return false;
    }
    public void startPolling(long intervalMillis) {
        if(polling.get()) {
            log.warn("Mail polling already active");
            return;
        }

        log.info("Starting Mail polling with interval: {} ms", intervalMillis);
        polling.set(true);

        // Create scheduled executor for polling
        pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mail - polling-" + config.getMailServerHost());
            t.setDaemon(true);
            return t;
        });

        // Schedule polling task
        pollingExecutor.scheduleWithFixedDelay(() -> {
            if(!polling.get()) {
                return;
            }

            try {
                log.debug("Executing Mail polling cycle");
                AdapterOperationResult result = pollForEmails();

                // If we have a callback and found emails, notify
                if(dataCallback != null && result.isSuccess() && result.getData() != null) {
                    List<Map<String, Object>> emails = (List<Map<String, Object>>) result.getData();
                    if(!emails.isEmpty()) {
                        log.info("Mail polling retrieved {} emails", emails.size());
                        dataCallback.onDataReceived(emails, result);
                    }
                }
            } catch(Exception e) {
                log.error("Error during Mail polling", e);
                if(dataCallback != null) {
                    dataCallback.onDataReceived(null,
                        AdapterOperationResult.failure("Polling error: " + e.getMessage()));
                }
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);

        log.info("Mail polling started successfully");
    }

    public void stopPolling() {
        if(polling.compareAndSet(true, false)) {
            log.info("Stopping Mail polling");

            if(pollingExecutor != null) {
                pollingExecutor.shutdown();
                try {
                    if(!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        pollingExecutor.shutdownNow();
                    }
                } catch(InterruptedException e) {
                    log.warn("Interrupted while waiting for polling executor to shutdown");
                    pollingExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                pollingExecutor = null;
            }

            log.info("Mail polling stopped");
        }
    }

    public void setDataReceivedCallback(DataReceivedCallback callback) {
        this.dataCallback = callback;
        log.debug("Data callback registered for Mail adapter");
    }

    public boolean isPolling() {
        return polling.get();
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.MAIL)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("Mail inbound adapter - receives emails from mail servers")
                .version("1.0.0")
                .supportsBatch(false)
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    // Helper methods for connection testing
    private AdapterOperationResult performMailConnectionTest() {
        Store testStore = null;
        try {
            testStore = createMailStore();
            testStore.connect(config.getMailUsername(), config.getMailPassword());

            if(testStore.isConnected()) {
                return AdapterOperationResult.success(
                        "Mail Connection", "Successfully connected to mail server");
            } else {
                return AdapterOperationResult.failure(
                        "Mail Connection", "Mail server connection failed");
            }
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Mail Connection", "Failed to connect to mail server: " + e.getMessage());
        } finally {
            if(testStore != null && testStore.isConnected()) {
                try {
                    testStore.close();
                } catch(Exception ignored) {
                }
            }
        }
    }

    private AdapterOperationResult performFolderAccessTest() {
        Store testStore = null;
        Folder testFolder = null;
        try {
            testStore = createMailStore();
            testStore.connect(config.getMailUsername(), config.getMailPassword());
            testFolder = testStore.getFolder(config.getFolderName());
            testFolder.open(Folder.READ_ONLY);

            int messageCount = testFolder.getMessageCount();
            return AdapterOperationResult.success(
                    "Folder Access", "Successfully accessed folder with " + messageCount + " messages");
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Folder Access", "Failed to access mail folder: " + e.getMessage());
        } finally {
            if(testFolder != null && testFolder.isOpen()) {
                try {
                    testFolder.close(false);
                } catch(Exception ignored) {
                }
            }
            if(testStore != null && testStore.isConnected()) {
                try {
                    testStore.close();
                } catch(Exception ignored) {
                }
            }
        }
    }

    private AdapterOperationResult performMessageFilteringTest() {
        try {
            SearchTerm searchTerm = buildSearchCriteria();
            String filterInfo = searchTerm != null ? "Message filters configured" : "No filters configured";
            return AdapterOperationResult.success(
                    "Message Filtering", filterInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Message Filtering", "Failed to build message filters: " + e.getMessage());
        }
    }
    public long getPollingInterval() {
        String interval = config.getPollingInterval();
        return interval != null && !interval.isEmpty() ? Long.parseLong(interval) : 30000L;
    }

    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.MAIL;
    }


    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }
}
