package com.akkafun.base.exception;

import com.akkafun.base.api.Error;

/**
 * hystrix会忽略这个异常, 不会触发熔断
 * Created by liubin on 2016/5/3.
 */
public class RemoteCallException extends AppBusinessException {

    private Error originError;

    public RemoteCallException(Error error, int httpStatus) {
        super(error.getCode(), httpStatus, "调用远程服务异常, cause: " + error.getMessage());
        this.originError = error;
    }

    public Error getOriginError() {
        return originError;
    }
}
