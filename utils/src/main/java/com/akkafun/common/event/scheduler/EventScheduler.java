package com.akkafun.common.event.scheduler;

import com.akkafun.common.event.service.EventBus;
import com.akkafun.common.scheduler.SchedulerTask;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by liubin on 2016/4/19.
 */
public class EventScheduler{

    EventBus eventBus;

    public EventScheduler(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    //    private static EventScheduler INSTANCE = new EventScheduler();
//
//    public static EventScheduler getInstance() {
//        return INSTANCE;
//    }
//
//    private EventScheduler() {
//        super();
//    }

    @Scheduled(fixedDelay = 3000L)
    public void doRun() {
        System.out.println("do run");

    }
}
