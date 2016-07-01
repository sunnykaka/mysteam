package com.akkafun.common.event.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Created by liubin on 2016/6/30.
 */
@Service
public class EventHandlerExecutor {

    private static Logger logger = LoggerFactory.getLogger(EventHandlerExecutor.class);


    /**
     * 执行handler处理
     * @param supplier
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeEventHandler(Supplier<T> supplier){

        return supplier.get();

    }
}
