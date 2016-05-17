package com.akkafun.coupon.api;

/**
 * Created by liubin on 2016/5/6.
 */
public interface CouponUrl {

    String SERVICE_NAME = "COUPON";

    String SERVICE_HOSTNAME = "http://COUPON";

    String CHECK_VALID_URL = "/coupons/{couponId}/valid";

    String COUPON_LIST_URL = "/coupons";

    String USER_COUPON_LIST_URL = "/coupons/{userId}";

    static String buildUrl(String url) {
        return SERVICE_HOSTNAME + url;
    }

}
