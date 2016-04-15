package com.akkafun.base.event.domain;


import com.akkafun.base.event.constants.EventType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 子类必须定义static变量EVENT_TYPE
 * 例如: public static final EventType EVENT_TYPE = EventType.TEST_EVENT;
 * Created by liubin on 2016/4/8.
 */
public abstract class BaseEvent {

    protected String id;

    protected EventType type;

    protected LocalDateTime createTime;

    protected String source;

    public BaseEvent(EventType type) {
        this.type = type;
        this.id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEvent)) return false;
        BaseEvent baseEvent = (BaseEvent) o;
        return Objects.equals(id, baseEvent.id) &&
                Objects.equals(type, baseEvent.type) &&
                Objects.equals(createTime, baseEvent.createTime) &&
                Objects.equals(source, baseEvent.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, createTime, source);
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                ", source='" + source + '\'' +
                '}';
    }
}
