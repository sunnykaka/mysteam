package com.akkafun.user.api.events.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Created by liubin on 2016/4/8.
 */
public class UserCreated extends BaseEvent {

    public static final EventType EVENT_TYPE = EventType.USER_CREATED;

    private Long userId;

    private String username;

    private LocalDateTime registerTime;

    @JsonCreator
    public UserCreated(
            @JsonProperty("userId") Long userId,
            @JsonProperty("username") String username,
            @JsonProperty("registerTime") LocalDateTime registerTime) {
        super(EVENT_TYPE);
        this.userId = userId;
        this.username = username;
        this.registerTime = registerTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }
}
