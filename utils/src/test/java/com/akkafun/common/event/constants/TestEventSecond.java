package com.akkafun.common.event.constants;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class TestEventSecond extends BaseEvent {

    public static final EventType EVENT_TYPE = EventType.TEST_EVENT_SECOND;

    private String name;

    @JsonCreator
    public TestEventSecond(
            @JsonProperty("name") String name) {
        super(EVENT_TYPE);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestEventSecond)) return false;
        if (!super.equals(o)) return false;
        TestEventSecond that = (TestEventSecond) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "TestEventSecond{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}