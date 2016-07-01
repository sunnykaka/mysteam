package com.akkafun.base.event.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Created by liubin on 2016/6/3.
 */
public class FailureInfo {

    private FailureReason reason;

    private LocalDateTime failureTime;

    private String message;

    public FailureInfo(FailureReason reason, LocalDateTime failureTime) {
        this(reason, failureTime, null);
    }

    @JsonCreator
    public FailureInfo(
            @JsonProperty("reason") FailureReason reason,
            @JsonProperty("failureTime") LocalDateTime failureTime,
            @JsonProperty("message") String message) {
        this.reason = reason;
        this.failureTime = failureTime;
        this.message = message;
    }

    public FailureReason getReason() {
        return reason;
    }

    public void setReason(FailureReason reason) {
        this.reason = reason;
    }

    public LocalDateTime getFailureTime() {
        return failureTime;
    }

    public void setFailureTime(LocalDateTime failureTime) {
        this.failureTime = failureTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FailureInfo{" +
                "reason=" + reason +
                ", failureTime=" + failureTime +
                ", message='" + message + '\'' +
                '}';
    }


}
