package com.akkafun.common.test.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.AskEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AskTestEvent extends AskEvent {

    public static final EventType EVENT_TYPE = EventType.ASK_TEST_EVENT;

    @Override
    public EventType getType() {
        return EVENT_TYPE;
    }

    private String name;

    @JsonCreator
    public AskTestEvent(
            @JsonProperty("name") String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AskTestEvent{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}