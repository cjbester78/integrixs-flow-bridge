package com.integrixs.engine.impl;

import com.integrixs.adapters.core.AdapterException;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.FetchRequest;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.engine.AdapterExecutor;
import com.integrixs.shared.enums.AdapterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AdapterExecutor that uses the AdapterFactoryManager
 * to create and execute adapter operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterExecutorImpl implements AdapterExecutor {
    
    private final AdapterFactoryManager adapterFactoryManager;
    private final CommunicationAdapterRepository adapterRepository;
    
    @Override
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
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Fetch failed: " + result.getMessage());
            }
            
            return result.getData();
            
        } catch (Exception e) {
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
            if (context != null && !context.isEmpty()) {
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
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Send failed: " + result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error sending data to adapter {}: {}", adapterId, e.getMessage(), e);
            throw new RuntimeException("Failed to send data to adapter " + adapterId, e);
        }
    }
    
    @Override
    public WritableByteChannel getWritableChannel(String adapterId, Map<String, Object> config) {
        // TODO: Implement streaming support when needed
        throw new UnsupportedOperationException("WritableByteChannel not yet implemented");
    }
    
    @Override
    public OutputStream getOutputStream(String adapterId, Map<String, Object> config) {
        // TODO: Implement streaming support when needed
        throw new UnsupportedOperationException("OutputStream not yet implemented");
    }
    
    private CommunicationAdapter getAdapter(String adapterId) {
        return adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
    }
    
    private void validateInboundAdapter(CommunicationAdapter adapter) {
        if (!"INBOUND".equalsIgnoreCase(adapter.getDirection())) {
            throw new IllegalArgumentException(
                "Adapter " + adapter.getId() + " is not a inbound adapter (direction: " + adapter.getDirection() + ")"
            );
        }
    }
    
    private void validateOutboundAdapter(CommunicationAdapter adapter) {
        if (!"OUTBOUND".equalsIgnoreCase(adapter.getDirection())) {
            throw new IllegalArgumentException(
                "Adapter " + adapter.getId() + " is not a outbound adapter (direction: " + adapter.getDirection() + ")"
            );
        }
    }
    
    private AdapterConfiguration.AdapterTypeEnum mapAdapterType(AdapterType type) {
        // Map from shared enum to adapter configuration enum
        return switch (type) {
            case HTTP, HTTPS -> AdapterConfiguration.AdapterTypeEnum.HTTP;
            case REST -> AdapterConfiguration.AdapterTypeEnum.REST;
            case JDBC -> AdapterConfiguration.AdapterTypeEnum.JDBC;
            case FTP -> AdapterConfiguration.AdapterTypeEnum.FTP;
            case SFTP -> AdapterConfiguration.AdapterTypeEnum.SFTP;
            case JMS -> AdapterConfiguration.AdapterTypeEnum.JMS;
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
        } catch (Exception e) {
            log.error("Error parsing adapter configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid adapter configuration", e);
        }
    }
    
    private String convertToString(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return (String) data;
        }
        if (data instanceof byte[]) {
            return new String((byte[]) data, StandardCharsets.UTF_8);
        }
        if (data instanceof InputStream) {
            try (InputStream is = (InputStream) data;
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                is.transferTo(baos);
                return baos.toString(StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read InputStream", e);
            }
        }
        return data.toString();
    }
}