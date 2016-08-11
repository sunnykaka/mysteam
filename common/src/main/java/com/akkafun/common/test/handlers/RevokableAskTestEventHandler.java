package com.akkafun.common.test.handlers;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.common.event.handler.RevokableAskEventHandler;
import com.akkafun.common.test.domain.RevokableAskTestEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/15.
 */
public class RevokableAskTestEventHandler implements RevokableAskEventHandler<RevokableAskTestEvent> {


    public static final String SUCCESS_EVENT_NAME = "克尔苏加德";

    public static final List<RevokableAskTestEvent> events = new CopyOnWriteArrayList<>();
    public static final List<RevokableAskTestEvent> revokeEvents = new CopyOnWriteArrayList<>();


    @Override
    public void processRevoke(RevokableAskTestEvent originEvent, FailureInfo failureInfo) {
        revokeEvents.add(originEvent);
    }

    @Override
    public BooleanWrapper processRequest(RevokableAskTestEvent event) {
        events.add(event);
        return new BooleanWrapper(event.getName().equals(SUCCESS_EVENT_NAME));
    }
}
