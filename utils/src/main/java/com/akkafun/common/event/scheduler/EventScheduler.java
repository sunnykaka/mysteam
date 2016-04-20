package com.akkafun.common.event.scheduler;

import com.akkafun.common.event.service.EventBus;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by liubin on 2016/4/19.
 */
public class EventScheduler{

    EventBus eventBus;

    public EventScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Scheduled(fixedRate = 1000L)
    public void sendUnpublishedEvent() {
        eventBus.sendUnpublishedEvent();
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1000L)
    public void searchAndHandleUnprocessedEvent() {
        eventBus.searchAndHandleUnprocessedEvent();
    }
}
