package com.akkafun.order.context;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.event.config.EventConfiguration;
import com.akkafun.common.event.config.InitBindProducer;
import com.akkafun.common.scheduler.config.SchedulerConfiguration;
import com.akkafun.common.spring.BaseConfiguration;
import com.akkafun.common.spring.ServiceClientConfiguration;
import com.akkafun.common.spring.WebApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import({BaseConfiguration.class, EventConfiguration.class, SchedulerConfiguration.class,
        ServiceClientConfiguration.class, WebApplication.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public InitBindProducer initBindProducer() {

        InitBindProducer initBindProducer = new InitBindProducer();
        initBindProducer.addPreInitializeProducers(EventType.ASK_REDUCE_BALANCE);
        initBindProducer.addPreInitializeProducers(EventType.ASK_USE_COUPON);
        return initBindProducer;
    }



}