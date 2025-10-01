package com.integrixs.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/test")
public class TestMultipartController {

    private static final Logger log = LoggerFactory.getLogger(TestMultipartController.class);


    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> testMultipart(@RequestParam("files") MultipartFile[] files) {
        log.info("Test multipart endpoint called with {} files", files.length);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("fileCount", files.length);

        for(int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("name", file.getOriginalFilename());
            fileInfo.put("size", file.getSize());
            fileInfo.put("contentType", file.getContentType());
            response.put("file" + i, fileInfo);
        }

        return ResponseEntity.ok(response);
    }
}
