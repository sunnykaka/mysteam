package com.akkafun.base.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureReason;

/**
 * Created by liubin on 2016/6/3.
 */
public class RevokeAskEvent extends BaseEvent {

    private FailureReason reason;

    private Long askEventId;

    @Override
    public EventType getType() {
        return EventType.REVOKE_ASK;
    }

    public RevokeAskEvent(Long id, FailureReason reason, Long askEventId) {
        super(id);
        this.reason = reason;
        this.askEventId = askEventId;
    }

    public FailureReason getReason() {
        return reason;
    }

    public Long getAskEventId() {
        return askEventId;
    }

    @Override
    public String toString() {
        return "RevokeAskEvent{" +
                "reason=" + reason +
                ", askEventId=" + askEventId +
                "} " + super.toString();
    }
}
