package com.akkafun.order.service.gateway;

import com.akkafun.account.api.AccountUrl;
import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.exception.RemoteCallException;
import com.akkafun.product.api.ProductUrl;
import com.akkafun.product.api.dtos.ProductDto;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liubin on 2016/7/18.
 */
@Service
public class AccountGateway {

    protected Logger logger = LoggerFactory.getLogger(AccountGateway.class);

    @Autowired
    RestTemplate restTemplate;

    @HystrixCommand(ignoreExceptions = RemoteCallException.class)
    public boolean isBalanceEnough(Long userId, Long amount) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(AccountUrl.buildUrl(AccountUrl.CHECK_ENOUGH_BALANCE))
                .queryParam("balance", amount)
                .buildAndExpand(userId).encode().toUri();

        BooleanWrapper booleanWrapper = restTemplate.getForObject(uri, BooleanWrapper.class);

        return booleanWrapper.isSuccess();
    }
}
