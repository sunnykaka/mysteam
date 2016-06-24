package com.akkafun.base.event.constants;

/**
 * Created by liubin on 2016/4/13.
 */
public enum EventType {

    ASK_RESPONSE,

    REVOKE_ASK,


    //user service
    USER_CREATED,

    //order service
    ORDER_CREATE_PENDING,

    //account service
    ASK_REDUCE_BALANCE,

    //coupon service
    ASK_USE_COUPON,


    NOTIFY_FIRST_TEST_EVENT,

    NOTIFY_SECOND_TEST_EVENT,

    ASK_TEST_EVENT,

    REVOKABLE_ASK_TEST_EVENT;


    public static EventType valueOfIgnoreCase(String name) {
        if(name == null) return null;
        return valueOf(name.toUpperCase());
    }


}
