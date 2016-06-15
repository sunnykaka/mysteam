package com.akkafun.common.event.handlers;

import com.akkafun.common.event.domain.AskTestEvent;
import com.akkafun.common.event.handler.AskEventHandler;

/**
 * Created by liubin on 2016/6/15.
 */
public class AskTestEventHandler implements AskEventHandler<AskTestEvent> {

    @Override
    public boolean processRequest(AskTestEvent event) {
        return false;
    }

}
