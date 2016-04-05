package com.akkafun.user.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Created by liubin on 2016/4/5.
 */
@EnableBinding(Processor.class)
public class EventActivator {

    private static Logger logger = LoggerFactory.getLogger(EventActivator.class);

    @ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public Object transform(Object payload) {
        logger.info("Transformed by " + this.getClass().getSimpleName() + ": " + payload);
        return payload;
    }

}
