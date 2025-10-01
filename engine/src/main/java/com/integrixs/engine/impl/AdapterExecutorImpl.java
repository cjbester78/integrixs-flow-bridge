package com.integrixs.engine.impl;

import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.FetchRequest;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.port.StreamingAdapterPort;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.engine.AdapterExecutor;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AdapterExecutor that uses the AdapterFactoryManager
 * to create and execute adapter operations.
 */
@Component
public class AdapterExecutorImpl implements AdapterExecutor {

    private static final Logger log = LoggerFactory.getLogger(AdapterExecutorImpl.class);


    private final AdapterFactoryManager adapterFactoryManager;
    private final CommunicationAdapterSqlRepository adapterRepository;

    public AdapterExecutorImpl(AdapterFactoryManager adapterFactoryManager, CommunicationAdapterSqlRepository adapterRepository) {
        this.adapterFactoryManager = adapterFactoryManager;
        this.adapterRepository = adapterRepository;
    }

    public String fetchData(String adapterId) {
        Object data = fetchDataAsObject(adapterId);
        return convertToString(data);
    }

    @Override
    public Object fetchDataAsObject(String adapterId) {
        try {
            CommunicationAdapter adapter = getAdapter(adapterId);
            validateInboundAdapter(adapter);

            AdapterConfiguration.AdapterTypeEnum adapterType = mapAdapterType(adapter.getType());
            Map<String, Object> config = parseConfiguration(adapter.getConfiguration());

            InboundAdapterPort senderAdapter = adapterFactoryManager.createSender(adapterType, config);

            // Create fetch request
            FetchRequest fetchRequest = FetchRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .adapterId(adapterId)
                .parameters(config)
                .build();

            // Execute the fetch operation
            AdapterOperationResult result = senderAdapter.fetch(fetchRequest);

            if(!result.isSuccess()) {
                throw new RuntimeException("Fetch failed: " + result.getMessage());
            }

            return result.getData();

        } catch(Exception e) {
            log.error("Error fetching data from adapter {}: {}", adapterId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch data from adapter " + adapterId, e);
        }
    }

    @Override
    public void sendData(String adapterId, String payload) {
        sendData(adapterId, payload, null);
    }

    @Override
    public void sendData(String adapterId, String payload, Map<String, Object> context) {
        sendData(adapterId, (Object) payload, context);
    }

    @Override
    public void sendData(String adapterId, byte[] data) {
        sendData(adapterId, (Object) data, null);
    }

    @Override
    public void sendData(String adapterId, Object data) {
        sendData(adapterId, data, null);
    }

    private void sendData(String adapterId, Object data, Map<String, Object> context) {
        try {
            CommunicationAdapter adapter = getAdapter(adapterId);
            validateOutboundAdapter(adapter);

            AdapterConfiguration.AdapterTypeEnum adapterType = mapAdapterType(adapter.getType());
            Map<String, Object> config = parseConfiguration(adapter.getConfiguration());

            // Merge context into config if provided
            if(context != null && !context.isEmpty()) {
                config.putAll(context);
            }

            OutboundAdapterPort receiverAdapter = adapterFactoryManager.createReceiver(adapterType, config);

            // Create send request
            SendRequest sendRequest = SendRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .adapterId(adapterId)
                .payload(data)
                .parameters(config)
                .synchronous(true)
                .build();

            // Execute the send operation
            AdapterOperationResult result = receiverAdapter.send(sendRequest);

            if(!result.isSuccess()) {
                throw new RuntimeException("Send failed: " + result.getMessage());
            }

        } catch(Exception e) {
            log.error("Error sending data to adapter {}: {}", adapterId, e.getMessage(), e);
            throw new RuntimeException("Failed to send data to adapter " + adapterId, e);
        }
    }

    @Override
    public WritableByteChannel getWritableChannel(String adapterId, Map<String, Object> config) {
        try {
            CommunicationAdapter adapter = getAdapter(adapterId);
            validateOutboundAdapter(adapter);

            AdapterConfiguration.AdapterTypeEnum adapterType = mapAdapterType(adapter.getType());
            Map<String, Object> adapterConfig = parseConfiguration(adapter.getConfiguration());

            // Merge provided config with adapter config
            if(config != null && !config.isEmpty()) {
                adapterConfig.putAll(config);
            }

            // Create outbound adapter
            OutboundAdapterPort outboundAdapter = adapterFactoryManager.createReceiver(adapterType, adapterConfig);

            // Check if adapter supports streaming
            if(!(outboundAdapter instanceof StreamingAdapterPort)) {
                log.error("Adapter {} ({}) does not support streaming", adapterId, adapter.getType());
                return null;
            }

            StreamingAdapterPort streamingAdapter = (StreamingAdapterPort) outboundAdapter;

            // Check if adapter supports WritableByteChannel specifically
            if(!streamingAdapter.supportsWritableByteChannel()) {
                log.error("Adapter {} does not support WritableByteChannel, use OutputStream instead", adapterId);
                return null;
            }

            log.info("Creating WritableByteChannel for adapter: {} ( {})", adapter.getName(), adapter.getType());
            return streamingAdapter.getWritableChannel(adapterConfig);

        } catch(UnsupportedOperationException e) {
            throw e; // Re - throw as - is
        } catch(Exception e) {
            log.error("Error creating WritableByteChannel for adapter {}: {}", adapterId, e.getMessage(), e);
            throw new RuntimeException("Failed to create WritableByteChannel for adapter " + adapterId, e);
        }
    }

    @Override
    public OutputStream getOutputStream(String adapterId, Map<String, Object> config) {
        try {
            CommunicationAdapter adapter = getAdapter(adapterId);
            validateOutboundAdapter(adapter);

            AdapterConfiguration.AdapterTypeEnum adapterType = mapAdapterType(adapter.getType());
            Map<String, Object> adapterConfig = parseConfiguration(adapter.getConfiguration());

            // Merge provided config with adapter config
            if(config != null && !config.isEmpty()) {
                adapterConfig.putAll(config);
            }

            // Create outbound adapter
            OutboundAdapterPort outboundAdapter = adapterFactoryManager.createReceiver(adapterType, adapterConfig);

            // Check if adapter supports streaming
            if(!(outboundAdapter instanceof StreamingAdapterPort)) {
                // Fallback: Create a buffered OutputStream that collects data and sends when closed
                log.info("Adapter {} does not support native streaming, using buffered approach", adapterId);
                return new BufferedAdapterOutputStream(adapterId, outboundAdapter, adapterConfig);
            }

            StreamingAdapterPort streamingAdapter = (StreamingAdapterPort) outboundAdapter;

            log.info("Creating OutputStream for adapter: {} ( {})", adapter.getName(), adapter.getType());
            return streamingAdapter.getOutputStream(adapterConfig);

        } catch(Exception e) {
            log.error("Error creating OutputStream for adapter {}: {}", adapterId, e.getMessage(), e);
            throw new RuntimeException("Failed to create OutputStream for adapter " + adapterId, e);
        }
    }

    private CommunicationAdapter getAdapter(String adapterId) {
        return adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
    }

    private void validateInboundAdapter(CommunicationAdapter adapter) {
        if(!"INBOUND".equalsIgnoreCase(adapter.getDirection())) {
            throw new IllegalArgumentException(
                "Adapter " + adapter.getId() + " is not a inbound adapter(direction: " + adapter.getDirection() + ")"
           );
        }
    }

    private void validateOutboundAdapter(CommunicationAdapter adapter) {
        if(!"OUTBOUND".equalsIgnoreCase(adapter.getDirection())) {
            throw new IllegalArgumentException(
                "Adapter " + adapter.getId() + " is not a outbound adapter(direction: " + adapter.getDirection() + ")"
           );
        }
    }

    private AdapterConfiguration.AdapterTypeEnum mapAdapterType(AdapterType type) {
        // Map from shared enum to adapter configuration enum
        return switch(type) {
            case HTTP, HTTPS -> AdapterConfiguration.AdapterTypeEnum.HTTP;
            case REST -> AdapterConfiguration.AdapterTypeEnum.REST;
            case JDBC -> AdapterConfiguration.AdapterTypeEnum.JDBC;
            case FTP -> AdapterConfiguration.AdapterTypeEnum.FTP;
            case SFTP -> AdapterConfiguration.AdapterTypeEnum.SFTP;
            case IBMMQ -> AdapterConfiguration.AdapterTypeEnum.IBMMQ;
            case KAFKA -> AdapterConfiguration.AdapterTypeEnum.KAFKA;
            case SOAP -> AdapterConfiguration.AdapterTypeEnum.SOAP;
            case FILE -> AdapterConfiguration.AdapterTypeEnum.FILE;
            case MAIL, EMAIL -> AdapterConfiguration.AdapterTypeEnum.MAIL;
            default -> throw new IllegalArgumentException("Unsupported adapter type: " + type);
        };
    }

    private Map<String, Object> parseConfiguration(String configuration) {
        try {
            // Parse JSON configuration to Map
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(configuration, Map.class);
        } catch(Exception e) {
            log.error("Error parsing adapter configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid adapter configuration", e);
        }
    }

    private String convertToString(Object data) {
        if(data == null) {
            return null;
        }
        if(data instanceof String) {
            return(String) data;
        }
        if(data instanceof byte[]) {
            return new String((byte[]) data, StandardCharsets.UTF_8);
        }
        if(data instanceof InputStream) {
            try(InputStream is = (InputStream) data;
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                is.transferTo(baos);
                return baos.toString(StandardCharsets.UTF_8);
            } catch(Exception e) {
                throw new RuntimeException("Failed to read InputStream", e);
            }
        }
        return data.toString();
    }

    /**
     * Buffered OutputStream implementation for adapters that don't support native streaming
     * Collects data in memory and sends it when the stream is closed
     */
    private class BufferedAdapterOutputStream extends OutputStream {
        private final String adapterId;
        private final OutboundAdapterPort adapter;
        private final Map<String, Object> config;
        private final ByteArrayOutputStream buffer;
        private boolean closed = false;

        public BufferedAdapterOutputStream(String adapterId, OutboundAdapterPort adapter, Map<String, Object> config) {
            this.adapterId = adapterId;
            this.adapter = adapter;
            this.config = config;
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws java.io.IOException {
            ensureOpen();
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws java.io.IOException {
            ensureOpen();
            buffer.write(b, off, len);
        }

        @Override
        public void flush() throws java.io.IOException {
            ensureOpen();
            buffer.flush();
            // Note: We don't send data on flush, only on close
            // This prevents partial data from being sent
        }

        @Override
        public void close() throws java.io.IOException {
            if(closed) {
                return;
            }

            try {
                // Send the collected data
                byte[] data = buffer.toByteArray();
                if(data.length > 0) {
                    SendRequest sendRequest = SendRequest.builder()
                        .requestId(UUID.randomUUID().toString())
                        .adapterId(adapterId)
                        .payload(data)
                        .parameters(config)
                        .synchronous(true)
                        .build();

                    AdapterOperationResult result = adapter.send(sendRequest);

                    if(!result.isSuccess()) {
                        throw new java.io.IOException("Failed to send buffered data: " + result.getMessage());
                    }

                    log.debug("Successfully sent {} bytes through buffered stream to adapter {}",
                        data.length, adapterId);
                }
            } finally {
                closed = true;
                buffer.close();
            }
        }

        private void ensureOpen() throws java.io.IOException {
            if(closed) {
                throw new java.io.IOException("Stream is closed");
            }
        }
    }

    // Builder
    public static AdapterExecutorImplBuilder builder() {
        return new AdapterExecutorImplBuilder();
    }

    public static class AdapterExecutorImplBuilder {
        private AdapterFactoryManager adapterFactoryManager;
        private CommunicationAdapterSqlRepository adapterRepository;

        public AdapterExecutorImplBuilder adapterFactoryManager(AdapterFactoryManager adapterFactoryManager) {
            this.adapterFactoryManager = adapterFactoryManager;
            return this;
        }

        public AdapterExecutorImplBuilder adapterRepository(CommunicationAdapterSqlRepository adapterRepository) {
            this.adapterRepository = adapterRepository;
            return this;
        }

        public AdapterExecutorImpl build() {
            return new AdapterExecutorImpl(this.adapterFactoryManager, this.adapterRepository);
        }
    }
}
