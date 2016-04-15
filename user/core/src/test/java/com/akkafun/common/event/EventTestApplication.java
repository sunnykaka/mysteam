package com.akkafun.common.event;

import com.akkafun.common.spring.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import(BaseApplication.class)
public class EventTestApplication {
    @Bean
    public EventInit eventInit() {
        return new EventInit(EventHandler.getInstance());
    }

}