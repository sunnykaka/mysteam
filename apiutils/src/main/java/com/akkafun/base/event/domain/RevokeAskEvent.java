package com.akkafun.base.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;

/**
 * Created by liubin on 2016/6/3.
 */
public class RevokeAskEvent extends BaseEvent {

    private FailureInfo failureInfo;

    private Long askEventId;

    @Override
    public EventType getType() {
        return EventType.REVOKE_ASK;
    }

    public RevokeAskEvent(FailureInfo failureInfo, Long askEventId) {
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
