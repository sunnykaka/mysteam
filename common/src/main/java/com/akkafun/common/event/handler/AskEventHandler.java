package com.akkafun.common.event.handler;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.event.domain.AskEvent;

/**
 * Created by liubin on 2016/6/3.
 */
public interface AskEventHandler<E extends AskEvent> {

    BooleanWrapper processRequest(E event);

}
