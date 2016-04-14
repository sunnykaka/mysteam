package com.akkafun.common.exception;

import com.akkafun.base.exception.BaseException;

/**
 * Created by liubin on 2016/4/14.
 */
public class EventException extends BaseException {

    public EventException(String message) {
        super(message);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventException(Throwable cause) {
        super(cause);
    }
}
