package com.akkafun.common.test.handlers;

import com.akkafun.common.event.handler.NotifyEventHandler;
import com.akkafun.common.test.domain.NotifyFirstTestEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/15.
 */
public class NotifyFirstTestEventSecondHandler implements NotifyEventHandler<NotifyFirstTestEvent> {

    public static final List<NotifyFirstTestEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void notify(NotifyFirstTestEvent event) {
        events.add(event);
    }

}
