package com.akkafun.user.api;

/**
 * Created by liubin on 2016/3/30.
 */
public interface UserUrl {

    String SERVICE_NAME = "USER";

    String SERVICE_HOSTNAME = "http://USER";

    String USER_REGISTER_URL = "/users/register";

    static String buildUrl(String url) {
        return SERVICE_HOSTNAME + url;
    }

}
