package com.akkafun.coupon.api.constants;

/**
 * Created by liubin on 2016/4/26.
 */
public enum CouponState {

    VALID("有效"),

    USED("已使用"),

    INVALID("已失效");

    public String desc;

    CouponState(String desc) {
        this.desc = desc;
    }



}
