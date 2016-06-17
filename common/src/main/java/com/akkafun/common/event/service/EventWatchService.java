package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.common.event.AskEventCallback;
import com.akkafun.common.event.AskParameter;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.constant.AskEventStatus;
import com.akkafun.common.event.dao.AskRequestEventPublishRepository;
import com.akkafun.common.event.dao.EventWatchRepository;
import com.akkafun.common.event.domain.AskRequestEventPublish;
import com.akkafun.common.event.domain.EventWatch;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/6/6.
 */
@Service
public class EventWatchService {

    private static Logger logger = LoggerFactory.getLogger(EventWatchService.class);

    @Autowired
    EventWatchRepository eventWatchRepository;

    @Autowired
    AskRequestEventPublishRepository askRequestEventPublishRepository;

    @Autowired
    EventPublishService eventPublishService;

    @Autowired
    EventRegistry eventRegistry;

    @Autowired
    EventBus eventBus;


    @Transactional
    public EventWatch watchAskEvents(AskParameter askParameter) {

        EventWatch eventWatch = new EventWatch();
        eventWatch.setAskEventStatus(AskEventStatus.PENDING);
        eventWatch.setAskEventIds(askParameter.getAskEvents().stream()
                .map(AskEvent::getId).collect(Collectors.toList()));
        if(askParameter.getCallbackClass() != null) {
            eventWatch.setCallbackClass(askParameter.getCallbackClass().getName());
        }
        if(!askParameter.getExtraParams().isEmpty()) {
            String json = JsonUtils.object2Json(askParameter.getExtraParams());
            eventWatch.setExtraParams(json);
        }
        eventWatch.setUnited(askParameter.isUnited());

        eventWatchRepository.save(eventWatch);

        return eventWatch;
    }

    @Transactional
    public void processEventWatch(Long watchId, AskEventStatus triggerStatus, FailureInfo failureInfo) {

        /**
         *
         如果不为PENDING, 不做处理.
         如果为PENDING, 则根据AskResponseEvent的success是true还是false, 设置成SUCESS或FAILED. 然后根据watchId, 找到UnitedEventWatch.
         首先判断UnitedEventWatch的askEventStatus状态, 如果不为PENDING, 不做处理.
         如果为PENDING, 查询UnitedEventWatch的askEventIds列表, 根据这些askEvents的状态重新改变UnitedEventWatch的状态.
         改变逻辑:
         根据更新时间升序排列askEvents. 查询到第一个不为PENDING也不为SUCCESS状态的askEvent, 根据这个状态设置UnionEventWatch的状态, 并且触发失败逻辑.
         如果所有askEvents都为Success, 触发成功逻辑. 如果全为PENDING, 报错.
         成功逻辑: UnitedEventWatch状态设置为SUCCESS, 调用注册的回调函数SuccessCallback.
         失败逻辑: UnitedEventWatch状态设置为TIMEOUT/FAILED/CANCELLED. 调用注册的回调函数FailureCallback. 再次查询UnionEventWatch下所有的askEvents,判断他们的状态.
         如果为TIMEOUT/FAILED/CANCELLED, 不做处理.
         如果为PENDING/SUCCESS, 设置状态为TIMEOUT/FAILED/CANCELLED, 然后判断该askEvent是否实现了Revokable接口, 如果实现了, 需要发送RevokeAskEvent事件进行撤销操作.
         */


        EventWatch eventWatch = eventWatchRepository.findOne(watchId);
        if(eventWatch == null) {
            throw new EventException("根据ID没有找到EventWatch, watchId: " + watchId);
        }
        if(!eventWatch.getAskEventStatus().equals(AskEventStatus.PENDING)) {
            return;
        }

        String callbackClassName = eventWatch.getCallbackClass();
        String extraParams = eventWatch.getExtraParams();
        List<Long> askEventIds = eventWatch.getAskEventIds();
        List<AskRequestEventPublish> askEvents = askEventIds.stream()
                .map(eventPublishService::getAskRequestEventByEventId)
                .collect(Collectors.toList());

        if(!eventWatch.isUnited()) {
            if(askEventIds.size() != 1) {
                throw new EventException("EventWatch united为true, 但是askEventIds的size不为1, watchId: " + watchId);
            }
            eventWatch.setAskEventStatus(triggerStatus);
            executeCallback(triggerStatus.equals(AskEventStatus.SUCCESS), callbackClassName, extraParams,
                    askEvents, failureInfo);
            eventWatchRepository.save(eventWatch);

        } else {

            if(askEvents.stream().allMatch(ep -> ep.getAskEventStatus().equals(AskEventStatus.SUCCESS))) {
                //所有askEvents都为Success, 触发成功逻辑
                eventWatch.setAskEventStatus(AskEventStatus.SUCCESS);
                executeCallback(true, callbackClassName, extraParams,
                        askEvents, failureInfo);
                eventWatchRepository.save(eventWatch);

            } else if(askEvents.stream().allMatch(ep -> ep.getAskEventStatus().equals(AskEventStatus.FAILED))) {
                //所有askEvents都为PENDING, 报错
                throw new EventException(String.format("处理united watch事件的时候发现askEvent对应的状态都为FAILED, " +
                        "程序有bug? watchId: %d, askEventIds: %s", watchId, askEventIds.toString()));

            } else {
                Optional<AskRequestEventPublish> failedEventPublish = askEvents.stream()
                        .sorted((o1, o2) -> {
                            //按updateTime升序排列
                            LocalDateTime o1Time = o1.getUpdateTime() == null ? o1.getCreateTime() : o1.getUpdateTime();
                            LocalDateTime o2Time = o2.getUpdateTime() == null ? o2.getCreateTime() : o2.getUpdateTime();
                            return o1Time.compareTo(o2Time);
                        })
                        .filter(ep -> !ep.getAskEventStatus().equals(AskEventStatus.PENDING)
                                && !ep.getAskEventStatus().equals(AskEventStatus.SUCCESS))
                        .findFirst();
                if(failedEventPublish.isPresent()) {
                    // 查询到第一个不为PENDING也不为SUCCESS状态的askEvent, 根据这个状态设置UnionEventWatch的状态
                    AskEventStatus failedStatus = failedEventPublish.get().getAskEventStatus();
                    eventWatch.setAskEventStatus(failedStatus);
                    FailureInfo unitedFailedInfo = new FailureInfo(EventUtils.fromAskEventStatus(failedStatus),
                            failedEventPublish.get().getUpdateTime());

                    eventWatchRepository.save(eventWatch);

                    //修改状态为PENDING或PENDING的askEvent到这个失败状态, 并且如果askEvent可以撤销, 进行撤销
                    askEvents.stream()
                            .filter(ep -> ep.getAskEventStatus().equals(AskEventStatus.PENDING)
                                    || ep.getAskEventStatus().equals(AskEventStatus.SUCCESS))
                            .forEach(ep -> {
                                ep.setAskEventStatus(failedStatus);
                                askRequestEventPublishRepository.save(ep);
                                if(eventRegistry.isEventRevokable(ep.getEventType())) {
                                    //撤销操作
                                    eventBus.publishRevokeEvent(ep.getEventId(), unitedFailedInfo);
                                }
                            });

                    // 执行失败的回调函数
                    executeCallback(false, callbackClassName, extraParams, askEvents, unitedFailedInfo);
                }

            }
        }

    }

    /**
     * 执行回调函数
     * @param success
     * @param callbackClassName
     * @param extraParams
     * @param askEvents
     * @param failureInfo
     */
    private void executeCallback(boolean success, String callbackClassName, String extraParams,
                                 List<AskRequestEventPublish> askEvents, FailureInfo failureInfo) {

        if(StringUtils.isNotBlank(callbackClassName)) {
            AskEventCallback askEventCallback = EventRegistry.getAskEventCallback(callbackClassName);

            askEventCallback.call(eventRegistry, success, askEvents, extraParams, failureInfo);
        }
    }
}
