package com.akkafun.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

public class EventInit implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(EventInit.class);

    private Object[] listeners;

    public EventInit(Object... listeners) {
        this.listeners = listeners;
    }

    public void register() {
        if(logger.isInfoEnabled()) {
            int size = listeners == null ? 0 : listeners.length;
            String listInfo = "";
            if(listeners != null) {
                for(Object listener : listeners) {
                    listInfo += ClassUtils.getUserClass(listener).getName() + ", ";
                }
            }
            logger.info(String.format("event listener register, size:%d, list: %s", size, listInfo));
        }
        if(listeners != null) {
            for(Object listener : listeners) {
                EventRegistry.getInstance().register(listener);
            }
            EventRegistry.getInstance().completeRegister();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        register();
    }
}
