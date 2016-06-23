package com.akkafun.account.handler;

import com.akkafun.account.service.AccountService;
import com.akkafun.common.event.handler.NotifyEventHandler;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.user.api.events.UserCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Created by liubin on 2016/4/14.
 */
public class UserCreatedHandler implements NotifyEventHandler<UserCreated> {

    protected Logger logger = LoggerFactory.getLogger(UserCreatedHandler.class);

    @Override
    public void notify(UserCreated event) {

        AccountService accountService = ApplicationContextHolder.context.getBean(AccountService.class);

        try {
            accountService.initAccount(event.getUserId());
        } catch (DataIntegrityViolationException e) {
            logger.warn(String.format("userId=%d的account在数据库已存在, errorMsg: %s",
                    event.getUserId(), e.getMessage()));
        }
    }
}
