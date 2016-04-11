package com.akkafun.user.api.events.domain;

import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.user.api.events.constants.UserEventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Created by liubin on 2016/4/8.
 */
public class UserCreated extends BaseEvent {

    private Long userId;

    private String username;

    private LocalDateTime registerTime;

    @JsonCreator
    public UserCreated(
            @JsonProperty("userId") Long userId,
            @JsonProperty("username") String username,
            @JsonProperty("registerTime") LocalDateTime registerTime) {
        super(UserEventType.USER_CREATED.name());
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
