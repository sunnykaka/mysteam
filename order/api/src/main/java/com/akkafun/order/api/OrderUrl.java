package com.akkafun.order.api;

/**
 * Created by liubin on 2016/5/6.
 */
public interface OrderUrl {

    String SERVICE_NAME = "ORDER";

    String SERVICE_HOSTNAME = "http://ORDER";

    String PLACE_ORDER = "/orders/place";

    String ORDER_INFO = "/orders/{orderId}";

    static String buildUrl(String url) {
        return SERVICE_HOSTNAME + url;
    }

}
