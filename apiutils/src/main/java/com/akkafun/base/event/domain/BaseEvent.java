package com.akkafun.base.event.domain;


import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by liubin on 2016/4/8.
 */
public abstract class BaseEvent {

    protected String id;

    protected String type;

    protected LocalDateTime createTime;

    protected String source;

    public BaseEvent(String type) {
        this.type = type.toLowerCase();
        createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
