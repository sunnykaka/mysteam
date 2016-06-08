package com.akkafun.base.event.domain;

/**
 * Created by liubin on 2016/6/3.
 */
public abstract class AskEvent extends BaseEvent {

    long ttl;

    public AskEvent() {
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

}
