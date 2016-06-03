package com.akkafun.base.event.constants;

/**
 * Created by liubin on 2016/4/13.
 */
public enum EventType {

    USER_CREATED,

    ORDER_CREATE_PENDING,

    TEST_EVENT_FIRST,

    TEST_EVENT_SECOND,

    ASK_RESPONSE,

    REVOKE_ASK;


    public static EventType valueOfIgnoreCase(String name) {
        if(name == null) return null;
        return valueOf(name.toUpperCase());
    }


}
