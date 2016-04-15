package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class TestEventFirst extends BaseEvent {

    public static final EventType EVENT_TYPE = EventType.TEST_EVENT_FIRST;

    private String name;

    private LocalDateTime registerTime;

    @JsonCreator
    public TestEventFirst(
            @JsonProperty("name") String name,
            @JsonProperty("registerTime") LocalDateTime registerTime) {
        super(EVENT_TYPE);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestEventFirst)) return false;
        if (!super.equals(o)) return false;
        TestEventFirst testEventFirst = (TestEventFirst) o;
        return Objects.equals(name, testEventFirst.name) &&
                Objects.equals(registerTime, testEventFirst.registerTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, registerTime);
    }

    @Override
    public String toString() {
        return "TestEventFirst{" +
                "name='" + name + '\'' +
                ", registerTime=" + registerTime +
                "} " + super.toString();
    }
}