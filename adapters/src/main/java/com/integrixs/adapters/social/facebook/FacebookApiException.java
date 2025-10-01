package com.integrixs.adapters.social.facebook;

/**
 * Facebook API exception
 */
public class FacebookApiException extends RuntimeException {
    public FacebookApiException(String message) {
        super(message);
    }

    public FacebookApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
