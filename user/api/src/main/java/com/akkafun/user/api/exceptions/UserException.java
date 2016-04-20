package com.akkafun.user.api.exceptions;

import com.akkafun.base.exception.BaseException;

/**
 * Created by liubin on 2016/4/19.
 */
public class UserException extends BaseException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    protected UserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
