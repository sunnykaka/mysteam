package com.akkafun.common.event.scheduler;

import com.akkafun.common.event.service.EventBus;
import com.akkafun.common.event.service.EventWatchService;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by liubin on 2016/4/19.
 */
public class EventScheduler{

    EventBus eventBus;

    public EventScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Scheduled(fixedRate = 300L)
    public void sendUnpublishedEvent() {
        eventBus.sendUnpublishedEvent();
    }

    @Scheduled(fixedRate = 300L)
    public void searchAndHandleUnprocessedEvent() {
        eventBus.searchAndHandleUnprocessedEvent();
    }

    @Scheduled(fixedRate = 300L)
    public void handleUnprocessedEventWatchProcess() {
        eventBus.handleUnprocessedEventWatchProcess();
    }

}
