package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.RfcAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * RfcAdapterServiceImpl - generated JavaDoc.
 */
public class RfcAdapterServiceImpl implements RfcAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(RfcAdapterServiceImpl.class);

    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received RFC for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.info("Sent RFC for adapter: {}", adapter.getName());
    }
}
