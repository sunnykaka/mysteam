package com.akkafun.common.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/4/15.
 */
public class EventHandler {

    private static final EventHandler INSTANCE = new EventHandler();

    private EventHandler(){}

    public static EventHandler getInstance() {
        return INSTANCE;
    }

    private List<TestEventFirst> firstEventList = new CopyOnWriteArrayList<>();

    private List<TestEventSecond> secondEventList = new CopyOnWriteArrayList<>();


    @Subscribe
    public synchronized void handleFirstEvent(TestEventFirst event) {

        firstEventList.add(event);

    }

    @Subscribe
    public synchronized void handleSecondEvent(TestEventSecond event) {

        secondEventList.add(event);

    }

    public List<TestEventFirst> getFirstEventList() {
        return firstEventList;
    }

    public List<TestEventSecond> getSecondEventList() {
        return secondEventList;
    }
}
