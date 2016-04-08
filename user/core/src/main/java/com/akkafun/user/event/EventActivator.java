package com.akkafun.user.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.ServiceActivator;

import java.nio.charset.Charset;

/**
 * Created by liubin on 2016/4/5.
 */
@EnableBinding(Processor.class)
public class EventActivator {

    private static Logger logger = LoggerFactory.getLogger(EventActivator.class);

    @ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public Object transform(Object payload) {
        byte[] bytes = (byte[]) payload;
        String message = new String(bytes, Charset.forName("UTF-8"));
        logger.info("Transformed by " + this.getClass().getSimpleName() + ": " + message);
        return payload;
    }

}
