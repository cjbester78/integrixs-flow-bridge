package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.RestAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * RestAdapterServiceImpl - generated JavaDoc.
 */
public class RestAdapterServiceImpl implements RestAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(RestAdapterServiceImpl.class);

    @Override
    public String get(CommunicationAdapter adapter) {
        return "GET from REST adapter: " + adapter.getName();
    }

    @Override
    public void post(CommunicationAdapter adapter, String payload) {
        logger.info("POST to REST adapter: {}", adapter.getName());
    }
}
