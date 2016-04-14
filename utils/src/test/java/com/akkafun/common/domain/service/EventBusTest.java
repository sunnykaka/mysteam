package com.akkafun.common.domain.service;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.EventUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/4/11.
 */
public class EventBusTest {


    @Test
    public void testSerialize() {

        TestEvent testEvent = new TestEvent("张三", LocalDateTime.now());

        String json = EventUtils.serializeEvent(testEvent);

        TestEvent testEventFromJson = EventUtils.deserializeEvent(json, TestEvent.class);

        assertThat(testEventFromJson, is(testEvent));


    }

    static class TestEvent extends BaseEvent {

        private static final EventType EVENT_TYPE = EventType.TEST_EVENT;

        private String name;

        private LocalDateTime registerTime;

        @JsonCreator
        public TestEvent(
                @JsonProperty("name") String name,
                @JsonProperty("registerTime") LocalDateTime registerTime) {
            super(EventType.TEST_EVENT);
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
            if (!(o instanceof TestEvent)) return false;
            if (!super.equals(o)) return false;
            TestEvent testEvent = (TestEvent) o;
            return Objects.equals(name, testEvent.name) &&
                    Objects.equals(registerTime, testEvent.registerTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name, registerTime);
        }

        @Override
        public String toString() {
            return "TestEvent{" +
                    "name='" + name + '\'' +
                    ", registerTime=" + registerTime +
                    "} " + super.toString();
        }
    }

}
