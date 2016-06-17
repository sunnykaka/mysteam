package com.akkafun.base.event.domain;


import com.akkafun.base.event.constants.EventType;
import com.sun.javafx.event.EventUtil;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 子类必须定义static变量EVENT_TYPE
 * 例如: public static final EventType EVENT_TYPE = EventType.TEST_EVENT;
 * Created by liubin on 2016/4/8.
 */
public abstract class BaseEvent {

    protected Long id;

    protected LocalDateTime createTime;

    public BaseEvent() {
        createTime = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public abstract EventType getType();



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEvent)) return false;
        BaseEvent baseEvent = (BaseEvent) o;
        return Objects.equals(id, baseEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
