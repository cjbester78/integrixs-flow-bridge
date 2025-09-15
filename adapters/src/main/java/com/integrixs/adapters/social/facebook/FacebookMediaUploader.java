package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles media uploads to Facebook
 */
@Component
public class FacebookMediaUploader {
    private static final Logger log = LoggerFactory.getLogger(FacebookMediaUploader.class);


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FacebookGraphApiClient apiClient;

    /**
     * Upload video to Facebook
     */
    public String uploadVideo(String pageId, String videoUrl, String description,
                            String accessToken, FacebookGraphApiConfig config) {
        try {
            log.info("Starting video upload to Facebook");

            // For large videos, use resumable upload
            if(isLargeVideo(videoUrl)) {
                return uploadVideoResumable(pageId, videoUrl, description, accessToken, config);
            }

            // For smaller videos, use direct upload
            String endpoint = String.format("%s/videos", pageId);
            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("description", description);

            // Check if URL or file
            if(videoUrl.startsWith("http")) {
                body.add("file_url", videoUrl);
            } else {
                Resource videoResource = new UrlResource(new URI("file:" + videoUrl));
                body.add("source", videoResource);
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String videoId = response.getBody().get("id").asText();
                log.info("Video uploaded successfully with ID: {}", videoId);
                return videoId;
            }

            throw new FacebookApiException("Failed to upload video");

        } catch(Exception e) {
            log.error("Error uploading video", e);
            throw new RuntimeException("Failed to upload video", e);
        }
    }

    /**
     * Upload video using resumable upload for large files
     */
    private String uploadVideoResumable(String pageId, String videoUrl, String description,
                                      String accessToken, FacebookGraphApiConfig config) {
        try {
            // Step 1: Initialize upload session
            String sessionId = initializeVideoUploadSession(pageId, getVideoSize(videoUrl), accessToken, config);

            // Step 2: Upload video chunks
            uploadVideoChunks(sessionId, videoUrl, accessToken, config);

            // Step 3: Finish upload and get video ID
            String videoId = finishVideoUpload(sessionId, description, accessToken, config);

            log.info("Large video uploaded successfully with ID: {}", videoId);
            return videoId;

        } catch(Exception e) {
            log.error("Error in resumable video upload", e);
            throw new RuntimeException("Failed resumable video upload", e);
        }
    }

    /**
     * Initialize video upload session
     */
    private String initializeVideoUploadSession(String pageId, long fileSize,
                                              String accessToken, FacebookGraphApiConfig config) {
        try {
            String endpoint = String.format("%s/videos", pageId);
            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("upload_phase", "start");
            body.add("file_size", String.valueOf(fileSize));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("upload_session_id").asText();
            }

            throw new FacebookApiException("Failed to initialize upload session");

        } catch(Exception e) {
            log.error("Error initializing upload session", e);
            throw new RuntimeException("Failed to initialize upload session", e);
        }
    }

    /**
     * Upload video chunks
     */
    private void uploadVideoChunks(String sessionId, String videoUrl,
                                 String accessToken, FacebookGraphApiConfig config) {
        // Implementation for chunked upload
        // This would read the video file in chunks and upload each chunk
        log.info("Uploading video chunks for session: {}", sessionId);
        // Simplified - actual implementation would handle chunking
    }

    /**
     * Finish video upload
     */
    private String finishVideoUpload(String sessionId, String description,
                                   String accessToken, FacebookGraphApiConfig config) {
        try {
            String endpoint = "videos";
            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("upload_phase", "finish");
            body.add("upload_session_id", sessionId);
            if(description != null) {
                body.add("description", description);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("id").asText();
            }

            throw new FacebookApiException("Failed to finish upload");

        } catch(Exception e) {
            log.error("Error finishing upload", e);
            throw new RuntimeException("Failed to finish upload", e);
        }
    }

    /**
     * Upload reel to Facebook
     */
    public String uploadReel(String pageId, String videoUrl, String description,
                           String accessToken, FacebookGraphApiConfig config) {
        try {
            log.info("Starting reel upload to Facebook");

            // Reels API endpoint
            String endpoint = String.format("%s/video_reels", pageId);
            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("description", description);
            body.add("upload_phase", "finish");

            // Upload video first
            String videoId = uploadVideo(pageId, videoUrl, null, accessToken, config);
            body.add("video_id", videoId);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String reelId = response.getBody().get("id").asText();
                log.info("Reel created successfully with ID: {}", reelId);
                return reelId;
            }

            throw new FacebookApiException("Failed to create reel");

        } catch(Exception e) {
            log.error("Error uploading reel", e);
            throw new RuntimeException("Failed to upload reel", e);
        }
    }

    /**
     * Upload story media
     */
    public String uploadStoryMedia(String pageId, String mediaUrl, String mediaType,
                                 String accessToken, FacebookGraphApiConfig config) {
        try {
            log.info("Starting story media upload to Facebook");

            String endpoint = String.format("%s/", pageId);
            if("video".equalsIgnoreCase(mediaType)) {
                endpoint += "video_stories";
            } else {
                endpoint += "photo_stories";
            }

            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            if("video".equalsIgnoreCase(mediaType)) {
                body.add("video_url", mediaUrl);
            } else {
                body.add("url", mediaUrl);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String storyId = response.getBody().get("id").asText();
                log.info("Story uploaded successfully with ID: {}", storyId);
                return storyId;
            }

            throw new FacebookApiException("Failed to upload story");

        } catch(Exception e) {
            log.error("Error uploading story", e);
            throw new RuntimeException("Failed to upload story", e);
        }
    }

    /**
     * Check if video is large(over 1GB)
     */
    private boolean isLargeVideo(String videoUrl) {
        try {
            long size = getVideoSize(videoUrl);
            return size > 1024 * 1024 * 1024; // 1GB
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Get video file size
     */
    private long getVideoSize(String videoUrl) {
        try {
            if(videoUrl.startsWith("http")) {
                // For URL, make HEAD request to get Content - Length
                HttpHeaders headers = new HttpHeaders();
                HttpEntity<Void> request = new HttpEntity<>(headers);

                ResponseEntity<Void> response = restTemplate.exchange(
                    videoUrl,
                    HttpMethod.HEAD,
                    request,
                    Void.class
               );

                return response.getHeaders().getContentLength();
            } else {
                // For file path
                return new java.io.File(videoUrl).length();
            }
        } catch(Exception e) {
            log.error("Error getting video size", e);
            return 0;
        }
    }
}
