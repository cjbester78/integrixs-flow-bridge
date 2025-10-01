package com.integrixs.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration for JavaMailSender
 */
@Configuration
public class MailConfig {

    @Value("${mail.smtp.host:smtp.gmail.com}")
    private String host;

    @Value("${mail.smtp.port:587}")
    private int port;

    @Value("${mail.smtp.username:}")
    private String username;

    @Value("${mail.smtp.password:}")
    private String password;

    @Value("${mail.smtp.auth:true}")
    private boolean auth;

    @Value("${mail.smtp.starttls-enable:true}")
    private boolean starttls;

    @Value("${mail.smtp.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${mail.smtp.timeout:10000}")
    private int timeout;

    @Value("${mail.smtp.write-timeout:10000}")
    private int writeTimeout;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        if (username != null && !username.isEmpty()) {
            mailSender.setUsername(username);
        }

        if (password != null && !password.isEmpty()) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.smtp.connectiontimeout", String.valueOf(connectionTimeout));
        props.put("mail.smtp.timeout", String.valueOf(timeout));
        props.put("mail.smtp.writetimeout", String.valueOf(writeTimeout));
        props.put("mail.debug", "false");

        return mailSender;
    }
}