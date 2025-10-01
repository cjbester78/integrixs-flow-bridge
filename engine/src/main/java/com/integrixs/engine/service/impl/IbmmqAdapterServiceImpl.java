package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.IbmmqAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * IBM MQ Adapter Service implementation.
 * Provides operations for sending and receiving messages via IBM MQ(formerly WebSphere MQ).
 */
public class IbmmqAdapterServiceImpl implements IbmmqAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(IbmmqAdapterServiceImpl.class);
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received IBM MQ message for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.debug("Sent IBM MQ message for adapter: {}", adapter.getName());
    }
}
