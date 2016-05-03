package com.akkafun.base.api;

/**
 * Created by liubin on 2016/5/3.
 */
public enum CommonErrorCode implements ErrorCode {

    BadRequest(400, "请求的参数个数或格式不符合要求"),
    InvalidArgument(400, "请求的参数不正确"),
    Unauthorized(401, "无权访问"),
    Forbidden(403, "禁止访问"),
    NotFound(404, "请求的地址不正确"),
    InternalError(500, "服务器内部错误");

    private int status;

    private String message;

    CommonErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
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
