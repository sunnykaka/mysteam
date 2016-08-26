package com.akkafun.order.service.gateway;

import com.akkafun.account.api.AccountUrl;
import com.akkafun.base.api.BooleanWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by liubin on 2016/8/25.
 */
@FeignClient(AccountUrl.SERVICE_HOSTNAME)
public interface AccountClient {

    @RequestMapping(method = RequestMethod.GET, value = AccountUrl.CHECK_ENOUGH_BALANCE)
    BooleanWrapper checkEnoughBalance(@PathVariable("userId") Long userId, @RequestParam("balance") Long balance);

}
