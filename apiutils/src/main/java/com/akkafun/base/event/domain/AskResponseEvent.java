package com.akkafun.base.event.domain;

import com.akkafun.base.event.constants.EventType;

/**
 * Created by liubin on 2016/6/3.
 */
public class AskResponseEvent extends BaseEvent {

    private boolean success;

    private Long askEventId;

    @Override
    public EventType getType() {
        return EventType.ASK_RESPONSE;
    }

    public AskResponseEvent(Long id, boolean success, Long askEventId) {
        super(id);
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
