package com.akkafun.order.service.gateway;

import com.akkafun.base.exception.RemoteCallException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by liubin on 2016/7/18.
 */
@Service
public class AccountGateway {

    protected Logger logger = LoggerFactory.getLogger(AccountGateway.class);

    @Autowired
    AccountClient accountClient;

    @HystrixCommand(ignoreExceptions = RemoteCallException.class)
    public boolean isBalanceEnough(Long userId, Long amount) {
        return accountClient.checkEnoughBalance(userId, amount).isSuccess();
    }

}

