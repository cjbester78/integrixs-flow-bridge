package com.integrixs.webserver.infrastructure.service;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import com.integrixs.webserver.domain.service.HttpClientService;
import com.integrixs.webserver.infrastructure.client.rest.RestClientImpl;
import com.integrixs.webserver.infrastructure.client.soap.SoapClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Implementation of HTTP client service
 */
@Service
public class HttpClientServiceImpl implements HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientServiceImpl.class);

    private final RestClientImpl restClient;
    private final SoapClientImpl soapClient;

    public HttpClientServiceImpl(RestClientImpl restClient, SoapClientImpl soapClient) {
        this.restClient = restClient;
        this.soapClient = soapClient;
    }

    @Override
    public OutboundResponse executeRestCall(OutboundRequest request) {
        return restClient.executeRestCall(request);
    }

    @Override
    public OutboundResponse executeSoapCall(OutboundRequest request) {
        return soapClient.executeSoapCall(request);
    }

    @Override
    public OutboundResponse executeGraphQLQuery(OutboundRequest request) {
        // GraphQL is essentially a POST request to a single endpoint
        // with the query in the payload
        if(request.getHttpMethod() == null) {
            request.setHttpMethod(OutboundRequest.HttpMethod.POST);
        }
        if(request.getContentType() == null) {
            request.setContentType("application/json");
        }

        return restClient.executeRestCall(request);
    }

    @Override
    public byte[] downloadFile(String url, Map<String, String> headers) {
        logger.info("Downloading file from: {}", url);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            // Add headers
            if(headers != null) {
                headers.forEach(connection::setRequestProperty);
            }

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to download file: HTTP " + responseCode);
            }

            // Read file content
            try(InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            }

        } catch(Exception e) {
            logger.error("Error downloading file from {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("File download failed", e);
        }
    }

    @Override
    public OutboundResponse uploadFile(String url, byte[] fileContent, String fileName, Map<String, String> headers) {
        logger.info("Uploading file {} to: {}", fileName, url);

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content - Type", "multipart/form - data; boundary = " + boundary);

            // Add custom headers
            if(headers != null) {
                headers.forEach(connection::setRequestProperty);
            }

            try(DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                // Write multipart form data
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content - Disposition: form - data; name = \"file\"; filename = \"" + fileName + "\"\r\n");
                outputStream.writeBytes("Content - Type: application/octet - stream\r\n\r\n");
                outputStream.write(fileContent);
                outputStream.writeBytes("\r\n");
                outputStream.writeBytes("--" + boundary + "--\r\n");
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection);

            if(responseCode >= 200 && responseCode < 300) {
                return OutboundResponse.success(requestId, responseCode, responseBody)
                        .withResponseTime(startTime);
            } else {
                return OutboundResponse.failure(requestId, responseCode, responseBody)
                        .withResponseTime(startTime);
            }

        } catch(Exception e) {
            logger.error("Error uploading file to {}: {}", url, e.getMessage(), e);
            return OutboundResponse.failure(requestId, 500, "Upload failed: " + e.getMessage())
                    .withResponseTime(startTime);
        }
    }

    @Override
    public void streamData(String url, StreamCallback callback) {
        logger.info("Starting data stream from: {}", url);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);

            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                callback.onError("Failed to open stream: HTTP " + responseCode);
                return;
            }

            try(InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                    callback.onData(chunk);
                }

                callback.onComplete();

            } catch(IOException e) {
                callback.onError("Stream error: " + e.getMessage());
            }

        } catch(Exception e) {
            logger.error("Error streaming data from {}: {}", url, e.getMessage(), e);
            callback.onError("Failed to open stream: " + e.getMessage());
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getResponseCode() >= 400
            ? connection.getErrorStream()
            : connection.getInputStream();

        if(inputStream == null) {
            return "";
        }

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }
}
