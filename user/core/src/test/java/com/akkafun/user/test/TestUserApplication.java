package com.akkafun.user.test;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.event.EventHandler;
import com.akkafun.common.event.EventInit;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.service.EventActivator;
import com.akkafun.common.spring.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import(BaseApplication.class)
public class TestUserApplication {

    @Bean
    public EventInit eventInit() {
        EventRegistry.getInstance().clear();
        return new EventInit(EventHandler.getInstance(), new com.akkafun.user.event.EventHandler());
    }


    /**
     * 测试EventBus.sendUnpublishedEvent中调用sendMessage抛出异常会不会导致整个事务回滚
     * @param eventActivator
     * @return
     */
    @Bean
    public EventActivator testEventActivator(EventActivator eventActivator) {

        return new EventActivator() {
            @Override
            public boolean sendMessage(String message, String destination) {
                //当遇到TEST_EVENT_SECOND事件时, 抛出异常
                if(destination.equals(EventType.TEST_EVENT_SECOND.toString())) {
                    throw new RuntimeException("我是异常");
                }
                return eventActivator.sendMessage(message, destination);
            }

            @Override
            public void receiveMessage(Object payload) {
                eventActivator.receiveMessage(payload);
            }
        };


    }

}