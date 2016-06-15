//package com.akkafun.common.event;
//
//import com.akkafun.common.event.constants.TestEventFirst;
//import com.akkafun.common.event.constants.TestEventSecond;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * Created by liubin on 2016/4/15.
// */
//public class EventHandler {
//
//    private static Logger logger = LoggerFactory.getLogger(EventHandler.class);
//
//    private static final EventHandler INSTANCE = new EventHandler();
//
//    private EventHandler(){}
//
//    public static EventHandler getInstance() {
//        return INSTANCE;
//    }
//
//    private List<TestEventFirst> firstEventList = new CopyOnWriteArrayList<>();
//
//    private List<TestEventSecond> secondEventList = new CopyOnWriteArrayList<>();
//
//
//    @Subscribe
//    public synchronized void handleFirstEvent(TestEventFirst event) {
//
//        logger.info(String.format("添加event[%s]到%s", event, "firstEventList"));
//        firstEventList.add(event);
//
//    }
//
//    @Subscribe
//    public synchronized void handleSecondEvent(TestEventSecond event) {
//
//        logger.info(String.format("添加event[%s]到%s", event, "secondEventList"));
//        secondEventList.add(event);
//
//    }
//
//    public List<TestEventFirst> getFirstEventList() {
//        return firstEventList;
//    }
//
//    public List<TestEventSecond> getSecondEventList() {
//        return secondEventList;
//    }
//}
