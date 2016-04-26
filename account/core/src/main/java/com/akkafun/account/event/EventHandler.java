package com.akkafun.account.event;

import com.akkafun.account.service.AccountService;
import com.akkafun.common.event.Subscribe;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.user.api.events.UserCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Created by liubin on 2016/4/14.
 */
public class EventHandler {

    protected Logger logger = LoggerFactory.getLogger(EventHandler.class);

    AccountService accountService = ApplicationContextHolder.context.getBean(AccountService.class);

    @Subscribe
    public void handleUserCreatedEvent(UserCreated event) {
        try {
            accountService.initAccount(event.getUserId());
        } catch (DataIntegrityViolationException e) {
            logger.warn(String.format("userId=%d的account在数据库已存在, errorMsg: %s",
                    event.getUserId(), e.getMessage()));
        }
    }

}
