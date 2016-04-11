package com.akkafun.common.event.service;

import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.dao.EventProcessRepository;
import com.akkafun.common.event.dao.EventPublishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by liubin on 2016/4/8.
 */
@Service
public class EventBus {

    @Autowired
    EventProcessRepository eventProcessRepository;

    @Autowired
    EventPublishRepository eventPublishRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(BaseEvent event) {




    }



}
