package com.akkafun.common.event.constant;

/**
 * Created by liubin on 2016/4/8.
 */
public enum ProcessStatus {

    NEW("未处理"),

    PROCESSED("已处理"),

    IGNORE("忽略");

    public String desc;

    ProcessStatus(String desc) {
        this.desc = desc;
    }

}
