package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.SoapAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * SoapAdapterServiceImpl - generated JavaDoc.
 */
public class SoapAdapterServiceImpl implements SoapAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(SoapAdapterServiceImpl.class);

    @Override
    public String invoke(CommunicationAdapter adapter) {
        return "Invoked SOAP adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.info("Sent to SOAP adapter: {}", adapter.getName());
    }
}
