package com.akkafun.common.event.constant;

/**
 * Created by liubin on 2016/4/8.
 */
public enum EventProcessStatus {

    NEW("未处理"),

    PROCESSED("已处理"),

    IGNORE("忽略");

    public String desc;

    EventProcessStatus(String desc) {
        this.desc = desc;
    }

}
