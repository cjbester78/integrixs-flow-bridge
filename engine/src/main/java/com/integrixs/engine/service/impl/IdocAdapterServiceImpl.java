package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.IdocAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * IdocAdapterServiceImpl - generated JavaDoc.
 */
public class IdocAdapterServiceImpl implements IdocAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(IdocAdapterServiceImpl.class);

    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received IDoc for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.info("Sent IDoc for adapter: {}", adapter.getName());
    }
}
