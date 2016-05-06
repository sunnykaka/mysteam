package com.akkafun.common.spring;

import com.akkafun.common.event.config.EventConfiguration;
import com.akkafun.common.spring.mvc.AppErrorController;
import com.akkafun.common.spring.mvc.AppExceptionHandlerController;
import com.akkafun.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by liubin on 2016/3/28.
 */
@EntityScan(basePackages = {
        "com.akkafun.**.domain",
        "org.springframework.data.jpa.convert.threeten"
})
@EnableJpaRepositories("com.akkafun.**.dao")
@EnableJpaAuditing
@ComponentScan({"com.akkafun.**.service", "com.akkafun.**.web"})
@Import({EventConfiguration.class})
public class BaseApplication {

    @Bean
    public ApplicationConstant applicationConstant() {
        return new ApplicationConstant();
    }

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return ApplicationContextHolder.getInstance();
    }

    //customize object mapper
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonUtils.OBJECT_MAPPER;
    }

}