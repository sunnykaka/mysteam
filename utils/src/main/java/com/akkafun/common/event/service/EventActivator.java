package com.akkafun.common.event.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import javax.validation.ConstraintViolationException;
import java.nio.charset.Charset;

/**
 * Created by liubin on 2016/4/5.
 */
@EnableBinding(Processor.class)
@DependsOn("bindingService")
public class EventActivator {
    private static Logger logger = LoggerFactory.getLogger(EventActivator.class);

    @Autowired
    private BinderAwareChannelResolver binderAwareChannelResolver;

    @Autowired
    private EventBus eventBus;

    @ServiceActivator(inputChannel = Processor.INPUT)
    public void receiveMessage(Object payload) {
        byte[] bytes = (byte[]) payload;
        String message = new String(bytes, Charset.forName("UTF-8"));
        logger.info("receiveMessage by " + this.getClass().getSimpleName() + ": " + message);

        try {
            eventBus.recordEvent(message);
        } catch (ConstraintViolationException e) {
            if(logger.isDebugEnabled()) {
                logger.debug("event[{}]在数据库已存在", message);
            }
        }

    }

    public boolean sendMessage(String message, String destination) {

        MessageChannel messageChannel = binderAwareChannelResolver.resolveDestination(destination);
        byte[] payload = message.getBytes(Charset.forName("UTF-8"));
        return messageChannel.send(MessageBuilder.withPayload(payload).build(), 1000L);

    }

}
