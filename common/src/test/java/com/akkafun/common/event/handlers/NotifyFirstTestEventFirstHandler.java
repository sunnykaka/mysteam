package com.akkafun.common.event.handlers;

import com.akkafun.common.event.domain.NotifyFirstTestEvent;
import com.akkafun.common.event.handler.NotifyEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/15.
 */
public class NotifyFirstTestEventFirstHandler implements NotifyEventHandler<NotifyFirstTestEvent> {

    public static final List<NotifyFirstTestEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void notify(NotifyFirstTestEvent event) {
        events.add(event);
    }

}
