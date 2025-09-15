package com.integrixs.engine.service;

import com.integrixs.data.model.CommunicationAdapter;

/**
 * IBM MQ Adapter Service interface.
 * Provides operations for sending and receiving messages via IBM MQ(formerly WebSphere MQ).
 */
public interface IbmmqAdapterService {
    String receive(CommunicationAdapter adapter);
    void send(CommunicationAdapter adapter, String payload);
}
