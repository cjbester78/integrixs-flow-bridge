package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.JdbcAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * JdbcAdapterServiceImpl - generated JavaDoc.
 */
public class JdbcAdapterServiceImpl implements JdbcAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAdapterServiceImpl.class);
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received JDBC data for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.info("Sent JDBC data for adapter: {}", adapter.getName());
    }
}
