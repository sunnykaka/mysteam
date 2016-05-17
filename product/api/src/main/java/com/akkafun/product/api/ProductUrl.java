package com.akkafun.product.api;

/**
 * Created by liubin on 2016/5/6.
 */
public interface ProductUrl {

    String SERVICE_NAME = "PRODUCT";

    String SERVICE_HOSTNAME = "http://PRODUCT";

    String ALL_PRODUCT_LIST_URL = "/products/all";

    String PRODUCT_LIST_URL = "/products";

    static String buildUrl(String url) {
        return SERVICE_HOSTNAME + url;
    }

}
