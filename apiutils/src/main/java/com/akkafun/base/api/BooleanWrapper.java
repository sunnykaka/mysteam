package com.akkafun.base.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by liubin on 2016/5/6.
 */
public class BooleanWrapper {

    private boolean success;

    private String message;

    public BooleanWrapper(boolean success) {
        this(success, null);
    }

    @JsonCreator
    public BooleanWrapper(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
