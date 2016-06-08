package com.akkafun.base.event.domain;

import com.akkafun.base.event.constants.EventType;

/**
 * Created by liubin on 2016/6/3.
 */
public class AskResponseEvent extends BaseEvent {

    public static final EventType EVENT_TYPE = EventType.ASK_RESPONSE;

    @Override
    public EventType getType() {
        return EVENT_TYPE;
    }

    private boolean success;

    private Long askEventId;

    public AskResponseEvent(boolean success, Long askEventId) {
        this.success = success;
        this.askEventId = askEventId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getAskEventId() {
        return askEventId;
    }

    @Override
    public String toString() {
        return "AskResponseEvent{" +
                "success=" + success +
                ", askEventId=" + askEventId +
                "} " + super.toString();
    }
}
