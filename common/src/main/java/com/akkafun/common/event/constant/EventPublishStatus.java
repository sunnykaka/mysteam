package com.akkafun.common.event.constant;

/**
 * Created by liubin on 2016/4/8.
 */
public enum EventPublishStatus {

    NEW("未发布"),

    PUBLISHED("已发布");

    public String desc;

    EventPublishStatus(String desc) {
        this.desc = desc;
    }

}
