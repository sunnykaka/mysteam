package com.akkafun.user.context;

import com.akkafun.common.event.EventInit;
import com.akkafun.common.scheduler.config.SchedulerConfiguration;
import com.akkafun.common.spring.BaseApplication;
import com.akkafun.user.event.EventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import({BaseApplication.class, SchedulerConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EventInit eventInit() {
        return new EventInit(new EventHandler());
    }

}