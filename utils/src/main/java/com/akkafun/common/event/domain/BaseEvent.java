package com.akkafun.common.event.domain;


import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by liubin on 2016/4/8.
 */
public abstract class BaseEvent {

    protected String id;

    protected String type;

    protected LocalDateTime createTime;

    protected String source;

    public BaseEvent(String type, String source) {
        this.type = type;
        this.source = source;
        id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public String getSource() {
        return source;
    }
}
