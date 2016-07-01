package com.akkafun.account.api;

/**
 * Created by liubin on 2016/5/6.
 */
public interface AccountUrl {

    String SERVICE_NAME = "ACCOUNT";

    String SERVICE_HOSTNAME = "http://ACCOUNT";

    String CHECK_ENOUGH_BALANCE = "/accounts/{userId}/enough";

    String ACCOUNT_BALANCE = "/accounts/{userId}/balance";

    String ACCOUNT_TRANSACTIONS = "/accounts/{userId}/transactions";

    static String buildUrl(String url) {
        return SERVICE_HOSTNAME + url;
    }


}
