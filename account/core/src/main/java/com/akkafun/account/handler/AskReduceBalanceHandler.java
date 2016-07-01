package com.akkafun.account.handler;

import com.akkafun.account.api.events.AskReduceBalance;
import com.akkafun.account.service.AccountService;
import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.event.handler.RevokableAskEventHandler;
import com.akkafun.common.spring.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liubin on 2016/6/24.
 */
public class AskReduceBalanceHandler implements RevokableAskEventHandler<AskReduceBalance> {

    private static Logger logger = LoggerFactory.getLogger(AskReduceBalanceHandler.class);

    @Override
    public void processRevoke(AskReduceBalance originEvent, FailureInfo failureInfo) {
        logger.debug("AskReduceBalanceHandler processRevoke, receive AskReduceBalance: " + originEvent);

        AccountService accountService = ApplicationContextHolder.context.getBean(AccountService.class);
        accountService.addBalance(originEvent.getUserId(), originEvent.getBalance());
    }

    @Override
    public BooleanWrapper processRequest(AskReduceBalance event) {
        logger.debug("AskReduceBalanceHandler processRequest, receive AskReduceBalance: " + event);

        if(event.getUserId() == null || event.getBalance() == null) {
            return new BooleanWrapper(false, "userId or balance is null");
        }

        AccountService accountService = ApplicationContextHolder.context.getBean(AccountService.class);
        accountService.reduceBalance(event.getUserId(), event.getBalance());
        return new BooleanWrapper(true);
    }



}
