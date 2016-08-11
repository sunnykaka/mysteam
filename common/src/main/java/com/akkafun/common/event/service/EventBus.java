package com.akkafun.common.event.service;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;
import com.akkafun.base.event.domain.*;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.event.AskParameter;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.constant.AskEventStatus;
import com.akkafun.common.event.constant.EventCategory;
import com.akkafun.common.event.constant.ProcessStatus;
import com.akkafun.common.event.dao.*;
import com.akkafun.common.event.domain.*;
import com.akkafun.common.event.handler.AskEventHandler;
import com.akkafun.common.event.handler.NotifyEventHandler;
import com.akkafun.common.event.handler.RevokableAskEventHandler;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/4/8.
 */
@Service
public class EventBus {

    private static Logger logger = LoggerFactory.getLogger(EventBus.class);

    @Autowired
    protected NotifyEventPublishRepository notifyEventPublishRepository;

    @Autowired
    protected AskRequestEventPublishRepository askRequestEventPublishRepository;

    @Autowired
    protected RevokeAskEventPublishRepository revokeAskEventPublishRepository;

    @Autowired
    protected AskResponseEventPublishRepository askResponseEventPublishRepository;

    @Autowired
    protected EventProcessRepository eventProcessRepository;

    @Autowired
    protected EventActivator eventActivator;

    @Autowired
    protected TaskExecutor taskExecutor;

    @Autowired
    protected EventRegistry eventRegistry;

    @Autowired
    protected EventWatchService eventWatchService;

    @Autowired
    protected EventPublishService eventPublishService;

    @Autowired
    protected EventHandlerExecutor eventHandlerExecutor;


    /**
     * 发布Notify事件
     * @param notifyEvent
     * @return
     */
    @Transactional
    public NotifyEventPublish publish(NotifyEvent notifyEvent) {

        fillEventId(notifyEvent);
        String payload = EventUtils.serializeEvent(notifyEvent);

        NotifyEventPublish eventPublish = new NotifyEventPublish();
        eventPublish.setPayload(payload);
        eventPublish.setEventId(notifyEvent.getId());
        eventPublish.setEventType(notifyEvent.getType());

        notifyEventPublishRepository.save(eventPublish);
        return eventPublish;
    }

    /**
     * 发布ask事件
     * @param askParameter
     * @return
     */
    @Transactional
    public List<AskRequestEventPublish> ask(AskParameter askParameter) {

        askParameter.getAskEvents().forEach(this::fillEventId);

        EventWatch eventWatch = eventWatchService.watchAskEvents(askParameter);

        return askParameter.getAskEvents().stream().map(askEvent -> {
            AskRequestEventPublish eventPublish = new AskRequestEventPublish();
            eventPublish.setEventId(askEvent.getId());
            eventPublish.setEventType(askEvent.getType());
            eventPublish.setAskEventStatus(AskEventStatus.PENDING);
            eventPublish.setWatchId(eventWatch.getId());
            eventPublish.setPayload(EventUtils.serializeEvent(askEvent));

            askRequestEventPublishRepository.save(eventPublish);

            return eventPublish;

        }).collect(Collectors.toList());
    }

    /**
     * 尝试对事件进行撤销
     * @param askEvent
     * @return
     */
    @Transactional
    public void revoke(AskEvent askEvent, FailureInfo failureInfo) {
        if(!(askEvent instanceof Revokable)) {
            throw new EventException(String.format("类型为%s的事件不能撤销", askEvent.getClass()));
        }
        if(askEvent.getId() == null) {
            throw new EventException("ID为空, 新事件不能撤销");
        }
        AskRequestEventPublish eventPublish = eventPublishService.getAskRequestEventByEventId(askEvent.getId());
        if(eventPublish.getStatus().equals(ProcessStatus.NEW)) {
            //首先判断原事件有没有发送, 如果没有发送就不发送了
            eventPublish.setStatus(ProcessStatus.IGNORE);
            askRequestEventPublishRepository.save(eventPublish);
        }

        if(eventPublish.getAskEventStatus().equals(AskEventStatus.PENDING)
                || eventPublish.getAskEventStatus().equals(AskEventStatus.SUCCESS)) {

            if(eventPublish.getStatus().equals(ProcessStatus.PROCESSED)) {
                publishRevokeEvent(askEvent.getId(), failureInfo);
            }

            //改变之前事件的状态
            AskEventStatus revokeAskEventStatus = AskEventStatus.FAILED;
            if(failureInfo != null && failureInfo.getReason() != null) {
                revokeAskEventStatus = EventUtils.fromFailureReason(failureInfo.getReason());
            }
            eventPublish.setAskEventStatus(revokeAskEventStatus);

            askRequestEventPublishRepository.save(eventPublish);

            //TODO AskRequestEventPublish 状态已经改变, 根据watchId判断eventWatch是不是也要改变

        }
    }

    /**
     * 发布撤销事件
     * @param askEventId
     * @param failureInfo
     */
    @Transactional
    public void publishRevokeEvent(Long askEventId, FailureInfo failureInfo) {
        RevokeAskEvent revokeAskEvent = new RevokeAskEvent(failureInfo, askEventId);
        fillEventId(revokeAskEvent);

        RevokeAskEventPublish revokeAskEventPublish = new RevokeAskEventPublish();
        revokeAskEventPublish.setAskEventId(askEventId);
        revokeAskEventPublish.setEventId(revokeAskEvent.getId());
        revokeAskEventPublish.setEventType(revokeAskEvent.getType());
        revokeAskEventPublish.setPayload(EventUtils.serializeEvent(revokeAskEvent));

        revokeAskEventPublishRepository.save(revokeAskEventPublish);
    }


    @SuppressWarnings("unchecked")
    @Transactional
    public void sendUnpublishedEvent() {

        List<EventPublish> events = eventPublishService.findUnpublishedEvent();
//        logger.info("待发布事件数量: " + events.size());

        for(EventPublish event : events) {
            try {
                //eventActivator.sendMessage抛异常不会导致整个事务回滚
                if(eventActivator.sendMessage(event.getPayload(), event.getEventType().name())) {
                    event.setStatus(ProcessStatus.PROCESSED);
                    saveEventPublish(event);
                }
            } catch (EventException e) {
                logger.error(e.getMessage());
            }  catch (Exception e) {
                logger.error(String.format("发送消息到队列的时候发生异常, EventPublish[id=%d, payload=%s]",
                                event.getId(), event.getPayload()), e);
            }

        }
    }

    private void saveEventPublish(EventPublish eventPublish) {

        if(eventPublish instanceof NotifyEventPublish) {
            notifyEventPublishRepository.save((NotifyEventPublish) eventPublish);
            notifyEventPublishRepository.getEm().flush();
        } else if(eventPublish instanceof AskRequestEventPublish) {
            askRequestEventPublishRepository.save((AskRequestEventPublish) eventPublish);
            askRequestEventPublishRepository.getEm().flush();
        } else if(eventPublish instanceof AskResponseEventPublish) {
            askResponseEventPublishRepository.save((AskResponseEventPublish) eventPublish);
            askResponseEventPublishRepository.getEm().flush();
        } else if(eventPublish instanceof RevokeAskEventPublish) {
            revokeAskEventPublishRepository.save((RevokeAskEventPublish) eventPublish);
            revokeAskEventPublishRepository.getEm().flush();
        } else {
            throw new EventException(String.format("unknown eventPublish class: %s, id: %d",
                    eventPublish.getClass(), eventPublish.getId()));
        }
    }


    @Transactional
    public void searchAndHandleUnprocessedEvent() {

        List<EventProcess> events = eventProcessRepository.findByStatus(ProcessStatus.NEW);
//        logger.info("待处理事件数量: " + events.size());
        CountDownLatch latch = new CountDownLatch(events.size());

        for(EventProcess event : events) {
            final Long eventProcessId = event.getId();
            taskExecutor.execute(() -> {
                try {
                    EventBus eventBus = ApplicationContextHolder.context.getBean(EventBus.class);
                    //handleEventProcess方法内报异常只回滚内部事务
                    eventBus.handleEventProcess(eventProcessId)
                            .map(eventWatchProcess -> eventWatchService.addToQueue(eventWatchProcess));
                } catch (EventException e) {
                    logger.error(e.getMessage());
                } catch (Exception e) {
                    logger.error(String.format("处理事件的时候发生异常, EventProcess[id=%d]",
                            eventProcessId), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            //等待事件异步处理完成
            latch.await();
        } catch (InterruptedException e) {
            logger.error("", e);
        }

    }

    @Transactional
    public Optional<EventWatchProcess> handleEventProcess(Long eventProcessId) {

        Optional<EventWatchProcess> eventWatchProcessOptional = Optional.empty();

        EventProcess eventProcess = eventProcessRepository.findOne(eventProcessId);
        if(!eventProcess.getStatus().equals(ProcessStatus.NEW)) {
            //已经被处理过了, 忽略
            return eventWatchProcessOptional;
        }

        logger.debug(String.format("handle event process, id: %d, event category: %s ",
                eventProcessId, eventProcess.getEventCategory()));

        switch (eventProcess.getEventCategory()) {
            case NOTIFY:
                processNotifyEvent(eventProcess);
                break;
            case ASK:
                processAskEvent(eventProcess);
                break;
            case REVOKE:
                processRevokeEvent(eventProcess);
                break;
            case ASKRESP:
                eventWatchProcessOptional = processAskResponseEvent(eventProcess);
                break;
            default:
                throw new EventException(String.format("unknown event category, process id: %d, event category: %s ",
                        eventProcessId, eventProcess.getEventCategory()));
        }

        eventProcess.setStatus(ProcessStatus.PROCESSED);
        eventProcessRepository.save(eventProcess);

        return eventWatchProcessOptional;
    }


    private void processNotifyEvent(EventProcess event) {

        EventType type = event.getEventType();

        Set<NotifyEventHandler> eventHandlers = eventRegistry.getNotifyEventHandlers(type);
        if(eventHandlers == null || eventHandlers.isEmpty()) {
            logger.error(String.format("EventProcess[id=%d, type=%s, payload=%s]的eventHandlers列表为空'",
                    event.getId(), type, event.getPayload()));
            return;
        }

        NotifyEvent notifyEvent = (NotifyEvent)eventRegistry.deserializeEvent(type, event.getPayload());

        eventHandlers.forEach(
                handler -> executeEventHandler(
                        event.getId(),
                        () -> {
                            handler.notify(notifyEvent);
                            return null;
                        },
                        null));

    }


    private void processAskEvent(EventProcess event) {

        EventType type = event.getEventType();

        Set<AskEventHandler> eventHandlers = eventRegistry.getAskEventHandlers(type);
        if(eventHandlers == null || eventHandlers.isEmpty()) {
            logger.error(String.format("EventProcess[id=%d, type=%s, payload=%s]的eventHandlers列表为空'",
                    event.getId(), type, event.getPayload()));
            return;
        }

        AskEvent askEvent = (AskEvent)eventRegistry.deserializeEvent(type, event.getPayload());

        eventHandlers.forEach(handler -> {
            EventHandlerResponse<BooleanWrapper> result = executeEventHandler(event.getId(),
                    () -> handler.processRequest(askEvent), new BooleanWrapper(false));
            createAskResponse(askEvent, result.getValue());
        });

    }

    private void processRevokeEvent(EventProcess event) {

        RevokeAskEvent revokeAskEvent = (RevokeAskEvent)eventRegistry.deserializeEvent(
                RevokeAskEvent.EVENT_TYPE,
                event.getPayload());

        EventProcess askEventProcess = eventProcessRepository.getByEventId(revokeAskEvent.getAskEventId());
        if(askEventProcess == null) {
            throw new EventException(String.format("根据事件ID[%d]没有找到EventProcess", revokeAskEvent.getAskEventId()));
        }

        EventType type = askEventProcess.getEventType();
        Set<RevokableAskEventHandler> eventHandlers = eventRegistry.getRevokableAskEventHandlers(type);
        if(eventHandlers == null || eventHandlers.isEmpty()) {
            logger.error(String.format("EventProcess[id=%d, type=%s, payload=%s]的eventHandlers列表为空'",
                    askEventProcess.getId(), type, askEventProcess.getPayload()));
            return;
        }

        AskEvent originEvent = (AskEvent) eventRegistry.deserializeEvent(type, askEventProcess.getPayload());

        eventHandlers.forEach(
                handler -> executeEventHandler(
                        event.getId(),
                        () -> {
                            handler.processRevoke(originEvent, revokeAskEvent.getFailureInfo());
                            return null;
                        },
                        null
                )
        );
    }

    private Optional<EventWatchProcess> processAskResponseEvent(EventProcess event) {

        AskResponseEvent askResponseEvent = eventRegistry.deserializeAskResponseEvent(event.getPayload());
        Long askEventId = askResponseEvent.getAskEventId();
        AskRequestEventPublish askRequestEventPublish = eventPublishService.getAskRequestEventByEventId(askEventId);
        if(!askRequestEventPublish.getAskEventStatus().equals(AskEventStatus.PENDING)) {
            return Optional.empty();
        }

        AskEventStatus askEventStatus;
        FailureInfo failureInfo = null;
        if(askResponseEvent.isSuccess()) {
            askEventStatus = AskEventStatus.SUCCESS;
        } else {
            askEventStatus = AskEventStatus.FAILED;
            failureInfo = new FailureInfo(FailureReason.FAILED, LocalDateTime.now());
        }
        askRequestEventPublish.setAskEventStatus(askEventStatus);
        askRequestEventPublishRepository.save(askRequestEventPublish);

        return eventWatchService.processEventWatch(askRequestEventPublish.getWatchId(), askEventStatus, failureInfo);

    }

    /**
     * 发送ask结果
     * @param askEvent
     * @param result
     * @return
     */
    private AskResponseEventPublish createAskResponse(AskEvent askEvent, BooleanWrapper result) {
        AskResponseEvent askResponseEvent = new AskResponseEvent(result.isSuccess(), result.getMessage(), askEvent.getId());
        fillEventId(askResponseEvent);
        AskResponseEventPublish eventPublish = new AskResponseEventPublish();
        eventPublish.setSuccess(result.isSuccess());
        eventPublish.setAskEventId(askEvent.getId());
        eventPublish.setEventType(AskResponseEvent.EVENT_TYPE);
        eventPublish.setEventId(askResponseEvent.getId());
        eventPublish.setPayload(EventUtils.serializeEvent(askResponseEvent));

        askResponseEventPublishRepository.save(eventPublish);

        return eventPublish;
    }


    private <T> EventHandlerResponse<T> executeEventHandler(Long eventProcessId, Supplier<T> supplier, T defaultValue){

        T value = defaultValue;
        String errorMessage = null;
        Stopwatch stopwatch = null;
        try {
            if(logger.isDebugEnabled()) {
                stopwatch = Stopwatch.createStarted();
            }
            //开启新事务, 防止handler执行方法报错导致整体事务回滚
            value = eventHandlerExecutor.executeEventHandler(supplier);
        } catch (TransactionSystemException ignore) {

        } catch (AppBusinessException e) {
            errorMessage = e.getMessage();
        } catch (Exception e) {
            logger.error("", e);
            errorMessage = e.getMessage();
        } finally {
            if(logger.isDebugEnabled() && stopwatch != null) {
                stopwatch.stop();
                logger.debug(String.format("执行事件回调结束耗时%dms, EventProcess[id=%d]",
                        stopwatch.elapsed(TimeUnit.MILLISECONDS), eventProcessId));
            }
        }

        return new EventHandlerResponse<>(value, errorMessage);
    }


    @Transactional
    public EventProcess recordEvent(String message) {
        Map<String, Object> eventMap = EventUtils.retrieveEventMapFromJson(message);
        EventType eventType = EventType.valueOfIgnoreCase((String) eventMap.get("type"));
        EventCategory eventCategory = eventRegistry.getEventCategoryByType(eventType);
        if(eventCategory.equals(EventCategory.ASKRESP) || eventCategory.equals(EventCategory.REVOKE)) {
            Long askEventId = (Long) eventMap.get("askEventId");
            if(askEventId == null) {
                throw new EventException("EventCategory为ASKRESP或REVOKE的事件, askEventId为null, payload: " + message);
            }
            boolean eventPublishNotExist = true;
            if(eventCategory.equals(EventCategory.ASKRESP)) {
                eventPublishNotExist = askRequestEventPublishRepository.getByEventId(askEventId) == null;
            } else if(eventCategory.equals(EventCategory.REVOKE)) {
                eventPublishNotExist = askResponseEventPublishRepository.countByAskEventId(askEventId) == 0L;
            }
            if(eventPublishNotExist) {
                //如果为ASKRESP或REVOKE事件并且请求id在数据库不存在, 则忽略这个事件
                return null;
            }
        }
        if(logger.isDebugEnabled()) {
            logger.debug("receive message from kafka: {}", message);
        }

        EventProcess eventProcess = new EventProcess();
        eventProcess.setPayload(message);
        eventProcess.setEventId((Long) eventMap.get("id"));
        eventProcess.setEventType(eventType);
        eventProcess.setEventCategory(eventCategory);

        eventProcessRepository.save(eventProcess);
        return eventProcess;
    }

    //不在这里加事务注解, 因为想让这个方法内对service的调用都是独立事务.
    public void handleUnprocessedEventWatchProcess() {
        List<EventWatchProcess> eventWatchProcessList = eventWatchService.findUnprocessedEventWatchProcess();
//        logger.info("待处理eventWatchProcess数量: " + eventWatchProcessList.size());
        Set<Long> successIdSet = new HashSet<>();
        Set<Long> watchIdSet = new HashSet<>();
        for(EventWatchProcess eventWatchProcess : eventWatchProcessList) {
            try {
                if(watchIdSet.add(eventWatchProcess.getWatchId())) {
                    //processUnitedEventWatch方法内报异常只回滚内部事务
                    eventWatchService.processUnitedEventWatch(eventWatchProcess);
                }
                successIdSet.add(eventWatchProcess.getId());
            } catch (EventException e) {
                logger.error(e.getMessage(), e);
                eventWatchService.addToQueue(eventWatchProcess);
                watchIdSet.remove(eventWatchProcess.getWatchId());
            } catch (Exception e) {
                logger.error("处理unitedEventWatch事件的时候发生异常, EventWatchProcessId:" + eventWatchProcess.getId(), e);
                eventWatchService.addToQueue(eventWatchProcess);
                watchIdSet.remove(eventWatchProcess.getWatchId());
            }
        }

        if(!successIdSet.isEmpty()) {
            eventWatchService.updateStatusBatchToProcessed(successIdSet.toArray(new Long[successIdSet.size()]));
        }
    }

    //不在这里加事务注解, 因为想让这个方法内对service的调用都是独立事务.
    public void handleTimeoutEventWatch() {
        LocalDateTime now = LocalDateTime.now();
        List<EventWatch> eventWatchList = eventWatchService.findTimeoutEventWatch(now);
        FailureInfo failureInfo = new FailureInfo(FailureReason.TIMEOUT, now);
        for(EventWatch eventWatch : eventWatchList) {
            try {
                eventWatchService.processEventWatch(eventWatch.getId(), AskEventStatus.TIMEOUT, failureInfo)
                        .map(eventWatchProcess -> eventWatchService.addToQueue(eventWatchProcess));
            } catch (EventException e) {
                logger.error(e.getMessage());
            }  catch (Exception e) {
                logger.error(String.format("处理超时EventWatch的时候发生异常, id=%d",
                        eventWatch.getId()), e);
            }
        }
    }


    public Long generateEventId() {
        //TODO generate id
        return Long.parseLong(RandomStringUtils.randomNumeric(18));
    }

    public void fillEventId(BaseEvent baseEvent) {
        if(baseEvent.getId() != null) {
            throw new EventException("event id不为空, id:" + baseEvent.getId());
        }
        baseEvent.setId(generateEventId());

    }


    public void setEventActivator(EventActivator eventActivator) {
        this.eventActivator = eventActivator;
    }



    private static class EventHandlerResponse<T> {

        private T value;

        String errorMessage;

        public EventHandlerResponse(T value, String errorMessage) {
            this.value = value;
            this.errorMessage = errorMessage;
        }

        public T getValue() {
            return value;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
