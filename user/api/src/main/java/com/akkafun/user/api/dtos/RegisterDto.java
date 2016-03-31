package com.akkafun.user.api.dtos;

import com.akkafun.user.api.utils.RegExpUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by liubin on 2016/3/29.
 */
public class RegisterDto {

    @NotNull(message = "用户名不能为空")
    @Pattern(regexp = RegExpUtils.USERNAME_REG_EXP, message = "用户名请输入2-20位，可由中文、英文或数字组成")
    private String username;

    @NotNull(message = "密码不能为空")
    @Min(value = 4, message = "密码长度需要为4-20位")
    @Max(value = 20, message = "密码长度需要为4-20位")
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
