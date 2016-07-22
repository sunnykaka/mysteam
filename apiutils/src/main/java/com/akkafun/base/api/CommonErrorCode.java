package com.akkafun.base.api;

/**
 * Created by liubin on 2016/5/3.
 */
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST(400, "请求的参数个数或格式不符合要求"),
    INVALID_ARGUMENT(400, "请求的参数不正确"),
    UNAUTHORIZED(401, "无权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "请求的地址不正确"),
    METHOD_NOT_ALLOWED(405, "不允许的请求方法"),
    NOT_ACCEPTABLE(406, "不接受的请求"),
    CONFLICT(409, "资源冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的Media Type"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "请求服务超时");

    private int status;

    private String message;

    CommonErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static CommonErrorCode fromHttpStatus(int httpStatus) {
        for(CommonErrorCode errorCode : values()) {
            if(errorCode.getStatus() == httpStatus) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }


    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
