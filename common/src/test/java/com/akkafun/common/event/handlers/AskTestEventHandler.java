package com.akkafun.common.event.handlers;

import com.akkafun.common.event.domain.AskTestEvent;
import com.akkafun.common.event.domain.NotifyFirstTestEvent;
import com.akkafun.common.event.handler.AskEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/15.
 */
public class AskTestEventHandler implements AskEventHandler<AskTestEvent> {

    public static final String SUCCESS_EVENT_NAME = "张三";

    public static final List<AskTestEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public boolean processRequest(AskTestEvent event) {
        events.add(event);
        return event.getName().equals(SUCCESS_EVENT_NAME);
    }

}
