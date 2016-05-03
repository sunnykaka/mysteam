package com.akkafun.user.api;

import com.akkafun.base.api.ErrorCode;

/**
 * Created by liubin on 2016/5/3.
 */
public enum UserErrorCode implements ErrorCode {

    UsernameExist(409, "用户名已存在"),
    PhoneExist(409, "手机已存在"),
    EmailExist(409, "邮箱已存在");

    private int status;

    private String message;

    UserErrorCode(int status, String message) {
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
