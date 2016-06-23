package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;
import com.akkafun.base.event.domain.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
            if (askEvent.getTtl() > 0L) {
                LocalDateTime timeoutTime = LocalDateTime.now()
                        .plus(askEvent.getTtl(), ChronoField.MILLI_OF_DAY.getBaseUnit());
                eventPublish.setTimeoutTime(timeoutTime);
            }
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
        revokeAskEventPublish.setAskEventId(revokeAskEvent.getId());
        revokeAskEventPublish.setEventType(revokeAskEvent.getType());
        revokeAskEventPublish.setPayload(EventUtils.serializeEvent(revokeAskEvent));

        revokeAskEventPublishRepository.save(revokeAskEventPublish);
    }


    @SuppressWarnings("unchecked")
    @Transactional
    public void sendUnpublishedEvent() {

        List<EventPublish> events = eventPublishService.findUnpublishedEvent();
        logger.info("待发布事件数量: " + events.size());

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
        logger.info("待处理事件数量: " + events.size());

        for(EventProcess event : events) {
            final Long eventProcessId = event.getId();
            taskExecutor.execute(() -> {
                EventBus eventBus = ApplicationContextHolder.context.getBean(EventBus.class);
                try {
                    //handleEventProcess方法内报异常只回滚内部事务
                    eventBus.handleEventProcess(eventProcessId);
                } catch (EventException e) {
                    logger.error(e.getMessage());
                } catch (Exception e) {
                    logger.error(String.format("处理事件的时候发生异常, EventProcess[id=%d]",
                            eventProcessId), e);
                }
            });

        }

    }

    @Transactional
    public void handleEventProcess(Long eventProcessId) {


        EventProcess eventProcess = eventProcessRepository.findOne(eventProcessId);
        if(!eventProcess.getStatus().equals(ProcessStatus.NEW)) {
            //已经被处理过了, 忽略
            return;
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
                processAskResponseEvent(eventProcess);
                break;
            default:
                throw new EventException(String.format("unknown event category, process id: %d, event category: %s ",
                        eventProcessId, eventProcess.getEventCategory()));
        }

        eventProcess.setStatus(ProcessStatus.PROCESSED);
        eventProcessRepository.save(eventProcess);

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

        final Long eventProcessId = event.getId();
        eventHandlers.forEach(
                handler -> executeEventHandler(
                        eventProcessId,
                        () -> handler.notify(notifyEvent)));

    }


    private void processAskEvent(EventProcess event) {

        EventType type = event.getEventType();

        Set<AskEventHandler> eventHandlerSet = eventRegistry.getAskEventHandlers(type);
        if(eventHandlerSet == null || eventHandlerSet.isEmpty()) {
            logger.error(String.format("EventProcess[id=%d, type=%s, payload=%s]的eventHandlers列表为空'",
                    event.getId(), type, event.getPayload()));
            return;
        }

        List<AskEventHandler> eventHandlers = new ArrayList<>(eventHandlerSet);
        AskEvent askEvent = (AskEvent)eventRegistry.deserializeEvent(type, event.getPayload());

        final Long eventProcessId = event.getId();
        eventHandlers.forEach(
                handler -> executeEventHandler(
                        eventProcessId,
                        () -> createAskResponse(askEvent, handler.processRequest(askEvent))));

    }

    private void processRevokeEvent(EventProcess event) {

        EventType type = event.getEventType();

        Set<RevokableAskEventHandler> eventHandlerSet = eventRegistry.getRevokableAskEventHandlers(type);
        if(eventHandlerSet == null || eventHandlerSet.isEmpty()) {
            logger.error(String.format("EventProcess[id=%d, type=%s, payload=%s]的eventHandlers列表为空'",
                    event.getId(), type, event.getPayload()));
            return;
        }

        List<RevokableAskEventHandler> eventHandlers = new ArrayList<>(eventHandlerSet);
        RevokeAskEvent revokeAskEvent = (RevokeAskEvent)eventRegistry.deserializeEvent(type, event.getPayload());

        final Long eventProcessId = event.getId();
        AskRequestEventPublish askRequestEventPublish = eventPublishService.getAskRequestEventByEventId(revokeAskEvent.getAskEventId());
        AskEvent originEvent = (AskEvent) eventRegistry.deserializeEvent(
                askRequestEventPublish.getEventType(),
                askRequestEventPublish.getPayload());

        eventHandlers.forEach(
                handler -> executeEventHandler(
                        eventProcessId,
                        () -> handler.processRevoke(originEvent, revokeAskEvent.getFailureInfo())
                )
        );
    }

    private void processAskResponseEvent(EventProcess event) {

        if(logger.isDebugEnabled()) {
            logger.debug("processAskResponseEvent: " + event);
        }
        AskResponseEvent askResponseEvent = eventRegistry.deserializeAskResponseEvent(event.getPayload());
        Long askEventId = askResponseEvent.getAskEventId();
        AskRequestEventPublish askRequestEventPublish = eventPublishService.getAskRequestEventByEventId(askEventId);
        if(!askRequestEventPublish.getAskEventStatus().equals(AskEventStatus.PENDING)) {
            return;
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

        eventWatchService.processEventWatch(askRequestEventPublish.getWatchId(), askEventStatus, failureInfo);


    }

    /**
     * 发送ask结果
     * @param askEvent
     * @param success
     * @return
     */
    private AskResponseEventPublish createAskResponse(AskEvent askEvent, boolean success) {
        AskResponseEvent askResponseEvent = new AskResponseEvent(success, askEvent.getId());
        fillEventId(askResponseEvent);
        AskResponseEventPublish eventPublish = new AskResponseEventPublish();
        eventPublish.setSuccess(success);
        eventPublish.setAskEventId(askEvent.getId());
        eventPublish.setEventType(AskResponseEvent.EVENT_TYPE);
        eventPublish.setEventId(askResponseEvent.getId());
        eventPublish.setPayload(EventUtils.serializeEvent(askResponseEvent));

        askResponseEventPublishRepository.save(eventPublish);

        return eventPublish;
    }


    public void executeEventHandler(Long eventProcessId, Runnable runnable){

        Stopwatch stopwatch = null;
        try {
            if(logger.isDebugEnabled()) {
                stopwatch = Stopwatch.createStarted();
                logger.debug(String.format("开始执行事件回调, EventProcess[id=%d]", eventProcessId));
            }
            runnable.run();
        } finally {
            if(logger.isDebugEnabled() && stopwatch != null) {
                stopwatch.stop();
                logger.debug(String.format("执行事件回调结束耗时%dms, EventProcess[id=%d]",
                        stopwatch.elapsed(TimeUnit.MILLISECONDS), eventProcessId));
            }
        }

    }


    @Transactional
    public EventProcess recordEvent(String message) {
        Map<String, Object> eventMap = EventUtils.retrieveEventMapFromJson(message);
        EventType eventType = EventType.valueOfIgnoreCase((String) eventMap.get("type"));
        EventCategory eventCategory = eventRegistry.getEventCategoryByType(eventType);
        if(eventCategory.equals(EventCategory.ASKRESP)) {
            Long askEventId = (Long) eventMap.get("askEventId");
            if(askEventId == null) {
                throw new EventException("EventCategory为ASKRESP的事件, askEventId为null, payload: " + message);
            }
            boolean eventPublishExist = askRequestEventPublishRepository.getByEventId(askEventId) != null;
            if(!eventPublishExist) {
                //如果为ASKRESP事件并且请求id在数据库不存在, 则忽略这个事件
                return null;
            }
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
        logger.info("待处理eventWatchProcess数量: " + eventWatchProcessList.size());
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
}
