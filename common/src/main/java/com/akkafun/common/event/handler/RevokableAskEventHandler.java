package com.akkafun.common.event.handler;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.base.event.domain.Revokable;

/**
 * Created by liubin on 2016/6/3.
 */
public interface RevokableAskEventHandler<E extends AskEvent & Revokable> extends AskEventHandler<E> {

    void processRevoke(E originEvent, FailureInfo failureInfo);

}
