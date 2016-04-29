package com.akkafun.order.api.constants;

/**
 * Created by liubin on 2016/4/26.
 */
public enum OrderStatus {

    CREATE_PENDING("正在下单"),

    CREATED("已下单"),

    CREATE_FAILED("下单失败");

    public String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }



}
