package com.akkafun.base.api;

/**
 * Created by liubin on 15-8-3.
 */
public class Error {

    private String code;

    private String message;

    private String requestUri;

    public Error(ErrorCode errorCode, String requestUri) {
        this(errorCode, requestUri, errorCode == null ? null : errorCode.getMessage());
    }

    public Error(ErrorCode errorCode, String requestUri, String message) {
        if(errorCode != null) {
            this.code = errorCode.getCode();
        }
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
}
