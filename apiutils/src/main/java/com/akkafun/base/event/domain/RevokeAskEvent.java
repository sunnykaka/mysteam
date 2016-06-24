package com.akkafun.base.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by liubin on 2016/6/3.
 */
public final class RevokeAskEvent extends BaseEvent {

    public static final EventType EVENT_TYPE = EventType.REVOKE_ASK;

    @Override
    public EventType getType() {
        return EVENT_TYPE;
    }

    private FailureInfo failureInfo;

    private Long askEventId;

    public RevokeAskEvent(
            @JsonProperty("failureInfo") FailureInfo failureInfo,
            @JsonProperty("askEventId") Long askEventId) {
        this.failureInfo = failureInfo;
        this.askEventId = askEventId;
    }

    public FailureInfo getFailureInfo() {
        return failureInfo;
    }

    public Long getAskEventId() {
        return askEventId;
    }

    @Override
    public String toString() {
        return "RevokeAskEvent{" +
                "failureInfo=" + failureInfo +
                ", askEventId=" + askEventId +
                "} " + super.toString();
    }
}
