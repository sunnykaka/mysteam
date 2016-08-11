package com.akkafun.common.test.handlers;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.common.event.handler.AskEventHandler;
import com.akkafun.common.test.domain.AskTestEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/15.
 */
public class AskTestEventHandler implements AskEventHandler<AskTestEvent> {

    public static final String SUCCESS_EVENT_NAME = "克尔苏加德";

    public static final List<AskTestEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public BooleanWrapper processRequest(AskTestEvent event) {
        events.add(event);
        return new BooleanWrapper(event.getName().equals(SUCCESS_EVENT_NAME));
    }

}
