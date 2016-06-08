package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.common.event.AskParameter;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.constant.AskEventStatus;
import com.akkafun.common.event.dao.EventPublishRepository;
import com.akkafun.common.event.dao.EventWatchRepository;
import com.akkafun.common.event.domain.EventPublish;
import com.akkafun.common.event.domain.EventWatch;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.utils.JsonUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liubin on 2016/6/6.
 */
@Service
public class EventWatchService {

    private static Logger logger = LoggerFactory.getLogger(EventWatchService.class);

    @Autowired
    EventWatchRepository eventWatchRepository;

    @Autowired
    EventPublishRepository eventPublishRepository;

    @Autowired
    EventRegistry eventRegistry;


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

    public void tryTriggerEvents(Long watchId, AskEventStatus triggerStatus, Optional<FailureInfo> failureInfo) {

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
        if(!eventWatch.isUnited()) {
            if(eventWatch.getAskEventIds().size() != 1) {
                throw new EventException("EventWatch united为true, 但是askEventIds的size不为1, watchId: " + watchId);
            }
            eventWatch.setAskEventStatus(triggerStatus);
            executeCallback(triggerStatus.equals(AskEventStatus.SUCCESS), eventWatch, failureInfo);


        }

    }

    private void executeCallback(boolean success, EventWatch eventWatch, Optional<FailureInfo> failureInfo) {

        String callbackClass = eventWatch.getCallbackClass();
        List<Long> askEventIds = eventWatch.getAskEventIds();
        String extraParams = eventWatch.getExtraParams();

        String callbackMethod = EventUtils.getAskCallbackMethodName(success);

        List<EventPublish> askEventPublishList = askEventIds.stream()
                .map(x -> {
                    EventPublish eventPublish = eventPublishRepository.getByEventId(x);
                    if(eventPublish == null) {
                        throw new EventException("根据eventId查询EventPublish为null, eventId: " + x);
                    }
                    return eventPublish;
                }).collect(Collectors.toList());

        List<Method> methods = Arrays.asList(callbackClass.getClass().getMethods());
        Stream<Method> successMethodStream = methods.stream()
                .filter(method -> EventUtils.SUCCESS_CALLBACK_NAME.equals(method.getName()));
        if(successMethodStream.count() > 1) {
            throw new EventException(String.format("回调类%s有%d个%s方法, 应该只能有1个",
                    callbackClass, successMethodStream.count(), EventUtils.SUCCESS_CALLBACK_NAME));
        }
        Optional<Method> successMethodOptional = successMethodStream.findFirst();
        if(!successMethodOptional.isPresent()) {
            throw new EventException(String.format("回调类%s中没有%s方法",
                    callbackClass, EventUtils.SUCCESS_CALLBACK_NAME));
        }
        Method successMethod = successMethodOptional.get();


    }
}
