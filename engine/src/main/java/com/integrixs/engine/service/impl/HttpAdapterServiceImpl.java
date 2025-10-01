package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.HttpAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * HttpAdapterServiceImpl - generated JavaDoc.
 */
public class HttpAdapterServiceImpl implements HttpAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(HttpAdapterServiceImpl.class);
    @Override
    public String get(CommunicationAdapter adapter) {
        return "GET from HTTP adapter: " + adapter.getName();
    }

    @Override
    public void post(CommunicationAdapter adapter, String payload) {
        logger.debug("POST to HTTP adapter: {}", adapter.getName());
    }
}
