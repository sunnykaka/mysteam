package com.akkafun.user.event;

import com.akkafun.common.event.Subscribe;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.user.api.events.UserCreated;
import com.akkafun.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liubin on 2016/4/14.
 */
public class EventHandler {

    protected Logger logger = LoggerFactory.getLogger(EventHandler.class);

    UserService userService = ApplicationContextHolder.context.getBean(UserService.class);

    @Subscribe
    public void handleUserCreatedEvent(UserCreated event) {
        logger.info(String.format("handleUserCreatedEvent call, userService is null: %s, event: %s",
                String.valueOf(userService == null), event));
    }

}
