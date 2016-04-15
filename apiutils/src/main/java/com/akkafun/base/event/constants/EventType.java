package com.akkafun.base.event.constants;

/**
 * Created by liubin on 2016/4/13.
 */
public enum EventType {

    USER_CREATED,

    TEST_EVENT_FIRST,

    TEST_EVENT_SECOND;


    public static EventType valueOfIgnoreCase(String name) {
        if(name == null) return null;
        return valueOf(name.toUpperCase());
    }


}
