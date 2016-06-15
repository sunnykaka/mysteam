package com.akkafun.common.event.handlers;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.common.event.domain.RevokableAskTestEvent;
import com.akkafun.common.event.handler.RevokableAskEventHandler;

/**
 * Created by liubin on 2016/6/15.
 */
public class RevokableAskTestEventHandler implements RevokableAskEventHandler<RevokableAskTestEvent> {

    @Override
    public void processRevoke(RevokableAskTestEvent originEvent, FailureInfo failureInfo) {

    }

    @Override
    public boolean processRequest(RevokableAskTestEvent event) {
        return false;
    }
}
