package com.akkafun.common.test.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.NotifyEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class NotifyFirstTestEvent extends NotifyEvent {

    public static final EventType EVENT_TYPE = EventType.NOTIFY_FIRST_TEST_EVENT;

    @Override
    public EventType getType() {
        return EVENT_TYPE;
    }

    private String name;

    private LocalDateTime registerTime;

    @JsonCreator
    public NotifyFirstTestEvent(
            @JsonProperty("name") String name,
            @JsonProperty("registerTime") LocalDateTime registerTime) {
        this.name = name;
        this.registerTime = registerTime;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    @Override
    public String toString() {
        return "NotifyFirstTestEvent{" +
                "name='" + name + '\'' +
                ", registerTime=" + registerTime +
                "} " + super.toString();
    }
}