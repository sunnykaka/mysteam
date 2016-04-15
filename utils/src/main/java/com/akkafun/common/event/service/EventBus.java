package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.EventSubscriber;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.constant.EventProcessStatus;
import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.dao.EventProcessRepository;
import com.akkafun.common.event.dao.EventPublishRepository;
import com.akkafun.common.event.domain.EventProcess;
import com.akkafun.common.event.domain.EventPublish;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by liubin on 2016/4/8.
 */
@Service
public class EventBus {

    private static Logger logger = LoggerFactory.getLogger(EventBus.class);

    @Autowired
    EventPublishRepository eventPublishRepository;

    @Autowired
    EventProcessRepository eventProcessRepository;

    @Autowired
    EventActivator eventActivator;

    @Autowired
    TaskExecutor taskExecutor;

    EventRegistry eventRegistry = EventRegistry.getInstance();

    @Transactional
    public EventPublish publish(BaseEvent event) {
        String payload = EventUtils.serializeEvent(event);

        EventPublish eventPublish = new EventPublish();
        eventPublish.setPayload(payload);

        eventPublishRepository.save(eventPublish);
        return eventPublish;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void sendUnpublishedEvent() {

        List<EventPublish> events = eventPublishRepository.findByStatus(EventPublishStatus.NEW);
        logger.debug("待发布事件数量: " + events.size());
        Set<Long> successEventSet = new HashSet<>();
        for(EventPublish event : events) {
            EventType type;
            try {
                type = EventUtils.retrieveEventTypeFromJson(event.getPayload());
            } catch (EventException e) {
                logger.error(
                        String.format("EventPublish payload格式不正确, EventPublish[id=%d], message: %s",
                                event.getId(), e.getMessage()));
                continue;
            }
            try {
                if(eventActivator.sendMessage(event.getPayload(), type.toString())) {
                    successEventSet.add(event.getId());
                }
                //TODO 测试eventActivator.sendMessage抛异常不会导致整个事务回滚
            } catch (Exception e) {
                logger.error(
                        String.format("发送消息到队列的时候发生异常, EventPublish[id=%d, payload=%s]",
                                event.getId(), event.getPayload()), e);
            }

        }
        logger.debug("成功发送事件数量: " + successEventSet.size());

        eventPublishRepository.updateStatusBatch(
                successEventSet.toArray(new Long[successEventSet.size()]),
                EventPublishStatus.PUBLISHED);

    }

    @Transactional
    public void searchAndHandleUnprocessedEvent() {

        List<EventProcess> events = eventProcessRepository.findByStatus(EventProcessStatus.NEW);
        logger.debug("待处理事件数量: " + events.size());
        for(EventProcess event : events) {
            EventType type;
            try {
                type = EventUtils.retrieveEventTypeFromJson(event.getPayload());
            } catch (EventException e) {
                logger.error(
                        String.format("EventProcess payload格式不正确, EventProcess[id=%d], message: %s",
                                event.getId(), e.getMessage()));
                continue;
            }

            Set<EventSubscriber> eventSubscriberSet = eventRegistry.findEventSubscriberByType(type);
            if(eventSubscriberSet == null || eventSubscriberSet.isEmpty()) {
                logger.error(
                        String.format("EventProcess[id=%d, type=%s, payload=%s] subscriber is null'",
                                event.getId(), type, event.getPayload()));
                continue;
            }

            try {
                List<EventSubscriber> eventSubscribers = new ArrayList<>(eventSubscriberSet);
                Class<? extends BaseEvent> eventClass = eventSubscribers.get(0).getEventClass();
                BaseEvent baseEvent = EventUtils.deserializeEvent(event.getPayload(), eventClass);

                final Long eventProcessId = event.getId();
                for(EventSubscriber subscriber : eventSubscribers) {
                    taskExecutor.execute(() -> {
                        try {
                            ApplicationContext context = ApplicationContextHolder.context;
                            EventBus eventBus = context.getBean(EventBus.class);
                            eventBus.handleEvent(baseEvent, eventProcessId, subscriber);
                        } catch (EventException e) {
                            logger.error(e.getMessage());
                        } catch (Exception e) {
                            logger.error(
                                    String.format("调用subscriber处理事件回调函数的时候发生异常, EventProcess[id=%d, payload=%s]",
                                            event.getId(), event.getPayload()), e);
                        }
                    });
                }
            } catch (Exception e) {
                logger.error(
                        String.format("处理事件的时候发生异常, EventProcess[id=%d, payload=%s]",
                                event.getId(), event.getPayload()), e);
            }

        }

    }

    @Transactional
    public void handleEvent(BaseEvent event, Long eventProcessId, EventSubscriber subscriber) throws InvocationTargetException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            logger.debug(String.format("开始执行事件回调, EventProcess[id=%d, type=%s]",
                    eventProcessId, event.getType()));
            subscriber.handleEvent(event);
            if(eventProcessRepository.updateStatusToProcessed(eventProcessId) == 0) {
                //更新数据库状态失败, 需要回滚
                throw new EventException(
                        String.format("执行事件回调函数之后, 更新数据库状态失败, 回滚事务. EventProcess[id=%d, type=%s]",
                                eventProcessId, event.getType()));
            }
        } finally {
            if(logger.isDebugEnabled()) {
                stopwatch.stop();
                logger.debug(String.format("执行事件回调结束耗时%dms, EventProcess[id=%d, type=%s]",
                        stopwatch.elapsed(TimeUnit.MILLISECONDS), eventProcessId, event.getType()));
            }
        }

    }

    @Transactional
    public EventProcess recordEvent(String message) {
        EventProcess eventProcess = new EventProcess();
        eventProcess.setPayload(message);
        eventProcessRepository.save(eventProcess);
        return eventProcess;
    }

}
