package com.akkafun.base.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by liubin on 15-8-3.
 */
public class Error {

    private String code;

    private String message;

    private String requestUri;

    @JsonCreator
    public Error(@JsonProperty("code") String code,
                 @JsonProperty("requestUri") String requestUri,
                 @JsonProperty(value = "message", defaultValue = "") String message) {
        this.code = code;
        this.requestUri = requestUri;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestUri() {
        return requestUri;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", requestUri='" + requestUri + '\'' +
                '}';
    }
}
