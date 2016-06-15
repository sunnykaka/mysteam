package com.akkafun.base.event.constants;

/**
 * Created by liubin on 2016/4/13.
 */
public enum EventType {

    USER_CREATED,

    ORDER_CREATE_PENDING,

    ASK_RESPONSE,

    REVOKE_ASK,



    NOTIFY_FIRST_TEST_EVENT,

    NOTIFY_SECOND_TEST_EVENT,

    ASK_TEST_EVENT,

    REVOKABLE_ASK_TEST_EVENT;


    public static EventType valueOfIgnoreCase(String name) {
        if(name == null) return null;
        return valueOf(name.toUpperCase());
    }


}
