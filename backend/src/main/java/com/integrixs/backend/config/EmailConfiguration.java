package com.integrixs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "mail.smtp")
public class EmailConfiguration {

    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username = "";
    private String password = "";
    private String from = "noreply@integrix.com";
    private boolean auth = true;
    private boolean starttlsEnable = true;
    private int connectionTimeout = 10000;
    private int timeout = 10000;
    private int writeTimeout = 10000;

    // Alert notification settings
    private boolean alertsEnabled = true;
    private String[] defaultAlertRecipients = new String[0];
    private int maxRecipientsPerEmail = 50;
    private boolean testMode = false;
    private String testEmailRecipient = "";

    // Getters and setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isStarttlsEnable() {
        return starttlsEnable;
    }

    public void setStarttlsEnable(boolean starttlsEnable) {
        this.starttlsEnable = starttlsEnable;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }

    public String[] getDefaultAlertRecipients() {
        return defaultAlertRecipients;
    }

    public void setDefaultAlertRecipients(String[] defaultAlertRecipients) {
        this.defaultAlertRecipients = defaultAlertRecipients;
    }

    public int getMaxRecipientsPerEmail() {
        return maxRecipientsPerEmail;
    }

    public void setMaxRecipientsPerEmail(int maxRecipientsPerEmail) {
        this.maxRecipientsPerEmail = maxRecipientsPerEmail;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public String getTestEmailRecipient() {
        return testEmailRecipient;
    }

    public void setTestEmailRecipient(String testEmailRecipient) {
        this.testEmailRecipient = testEmailRecipient;
    }
}
