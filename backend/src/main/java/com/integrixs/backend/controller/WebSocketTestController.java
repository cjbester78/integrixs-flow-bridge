package com.integrixs.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ws - test")
public class WebSocketTestController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTestController.class);


    @GetMapping("/status")
    public Map<String, Object> getWebSocketStatus() {
        log.info("WebSocket status check requested");
        Map<String, Object> status = new HashMap<>();
        status.put("websocketEnabled", true);
        status.put("endpoints", new String[] {"/ws/messages", "/ws/flow - execution", "/ws/flow - execution - native"});
        status.put("message", "WebSocket endpoints should be available");
        return status;
    }
}
