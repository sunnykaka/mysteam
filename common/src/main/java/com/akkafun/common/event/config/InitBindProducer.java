package com.akkafun.common.event.config;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.AskResponseEvent;
import com.akkafun.base.event.domain.RevokeAskEvent;
import com.akkafun.common.event.EventRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liubin on 2016/6/28.
 */
public class InitBindProducer implements InitializingBean {

    @Autowired
    private BinderAwareChannelResolver binderAwareChannelResolver;

    private Set<EventType> preInitializeProducers = new HashSet<>();

    public InitBindProducer() {
        preInitializeProducers.add(AskResponseEvent.EVENT_TYPE);
        preInitializeProducers.add(RevokeAskEvent.EVENT_TYPE);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        preInitializeProducers.stream().forEach(x -> binderAwareChannelResolver.resolveDestination(x.name()));
    }

    public void addPreInitializeProducers(EventType eventType) {
        preInitializeProducers.add(eventType);
    }

}
