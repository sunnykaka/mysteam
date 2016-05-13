package com.akkafun.integrationtest.test;

import com.akkafun.common.event.EventInit;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.spring.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import(BaseApplication.class)
public class IntegrationTestApplication {

    @Bean
    public EventInit eventInit() {
        EventRegistry.getInstance().clear();
        return new EventInit();
    }


}