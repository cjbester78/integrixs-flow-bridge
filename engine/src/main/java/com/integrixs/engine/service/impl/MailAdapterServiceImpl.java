package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.MailAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * MailAdapterServiceImpl - generated JavaDoc.
 */
public class MailAdapterServiceImpl implements MailAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(MailAdapterServiceImpl.class);

    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received email from adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.info("Sent email from adapter: {}", adapter.getName());
    }
}
