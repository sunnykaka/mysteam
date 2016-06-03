package com.akkafun.common.event.constant;

/**
 * Created by liubin on 2016/6/3.
 */
public enum AskEventStatus {

    PENDING("请求中"),

    TIMEOUT("已超时"),

    FAILED("已失败"),

    SUCCESS("完成"),

    CANCELLED("已取消");

    public String desc;

    AskEventStatus(String desc) {
        this.desc = desc;
    }

}
