package com.integrixs.engine.service.impl;

import com.integrixs.engine.service.SftpAdapterService;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
/**
 * SftpAdapterServiceImpl - generated JavaDoc.
 */
public class SftpAdapterServiceImpl implements SftpAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(SftpAdapterServiceImpl.class);

    @Override
    public String download(CommunicationAdapter adapter) {
        return "Downloaded SFTP data for adapter: " + adapter.getName();
    }

    @Override
    public void upload(CommunicationAdapter adapter, String payload) {
        logger.info("Uploaded to SFTP adapter: {}", adapter.getName());
    }
}
