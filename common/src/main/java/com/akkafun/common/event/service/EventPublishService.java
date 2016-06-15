package com.akkafun.common.event.service;

import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.dao.AskRequestEventPublishRepository;
import com.akkafun.common.event.dao.AskResponseEventPublishRepository;
import com.akkafun.common.event.dao.NotifyEventPublishRepository;
import com.akkafun.common.event.dao.RevokeAskEventPublishRepository;
import com.akkafun.common.event.domain.*;
import com.akkafun.common.exception.EventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubin on 2016/6/13.
 */
@Service
public class EventPublishService {

    @Autowired
    protected NotifyEventPublishRepository notifyEventPublishRepository;

    @Autowired
    protected AskRequestEventPublishRepository askRequestEventPublishRepository;

    @Autowired
    protected RevokeAskEventPublishRepository revokeAskEventPublishRepository;

    @Autowired
    protected AskResponseEventPublishRepository askResponseEventPublishRepository;


    @Transactional(readOnly = true)
    public List<EventPublish> findUnpublishedEvent() {
        List<EventPublish> unpublishedEvents = new ArrayList<>();
        unpublishedEvents.addAll(notifyEventPublishRepository.findByStatus(EventPublishStatus.NEW));
        unpublishedEvents.addAll(askRequestEventPublishRepository.findByStatus(EventPublishStatus.NEW));
        unpublishedEvents.addAll(revokeAskEventPublishRepository.findByStatus(EventPublishStatus.NEW));
        unpublishedEvents.addAll(askResponseEventPublishRepository.findByStatus(EventPublishStatus.NEW));
        return unpublishedEvents;
    }

    @Transactional(readOnly = true)
    public AskRequestEventPublish getAskRequestEventByEventId(Long eventId) {
        AskRequestEventPublish askRequestEventPublish = askRequestEventPublishRepository.getByEventId(eventId);
        if(askRequestEventPublish == null) {
            throw new EventException(String.format("根据事件ID[%d]没有找到AskRequestEventPublish", eventId));
        }
        return askRequestEventPublish;
    }


}
