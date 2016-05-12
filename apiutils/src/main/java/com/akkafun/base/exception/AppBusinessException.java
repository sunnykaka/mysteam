package com.akkafun.base.exception;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.api.ErrorCode;

/**
 * Created by liubin on 2016/5/3.
 */
public class AppBusinessException extends BaseException {

    //类似Http状态码
    private ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;


    public AppBusinessException(String message) {
        super(message);
    }

    /**
     * @param errorCode 状态码, 这个字段会在错误信息里返回给客户端.
     * @param message
     */
    public AppBusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppBusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public AppBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppBusinessException(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "AppBusinessException{" +
                "errorCode=" + errorCode +
                "} " + super.toString();
    }
}
