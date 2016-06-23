package com.akkafun.common.test;

import com.akkafun.common.event.service.EventBus;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by liubin on 2016/3/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseTest {

    @Autowired
    protected EventBus eventBus;

    /**
     * 发送事件并等待
     */
    protected void sendEvent() {

        eventBus.sendUnpublishedEvent();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 异步处理事件并等待
     */
    protected void handleEvent() {

        try {
            eventBus.searchAndHandleUnprocessedEvent();
            Thread.sleep(3000L);
            eventBus.handleUnprocessedEventWatchProcess();
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }



}
