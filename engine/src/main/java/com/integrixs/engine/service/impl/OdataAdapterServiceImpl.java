package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.OdataAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * OdataAdapterServiceImpl - generated JavaDoc.
 */
public class OdataAdapterServiceImpl implements OdataAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(OdataAdapterServiceImpl.class);
    @Override
    public String receive(CommunicationAdapter adapter) {
        return "Received OData for adapter: " + adapter.getName();
    }

    @Override
    public void send(CommunicationAdapter adapter, String payload) {
        logger.debug("Sent OData for adapter: {}", adapter.getName());
    }
}
