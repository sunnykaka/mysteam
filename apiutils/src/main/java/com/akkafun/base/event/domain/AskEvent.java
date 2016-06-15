package com.akkafun.base.event.domain;

import com.akkafun.base.Constants;

/**
 * Created by liubin on 2016/6/3.
 */
public abstract class AskEvent extends BaseEvent {

    public long getTtl() {
        return Constants.ASK_TIMEOUT;
    }

}
