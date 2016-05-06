package com.akkafun.common.spring;

import com.akkafun.common.spring.mvc.AppErrorController;
import com.akkafun.common.spring.mvc.AppExceptionHandlerController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;

/**
 * Created by liubin on 2016/3/28.
 */
public class WebApplication {

    //error page
    @Bean
    public ErrorController errorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        return new AppErrorController(errorAttributes, serverProperties.getError());
    }

    //exception handler
    @Bean
    public AppExceptionHandlerController appExceptionHandlerController() {
        return new AppExceptionHandlerController();
    }

}