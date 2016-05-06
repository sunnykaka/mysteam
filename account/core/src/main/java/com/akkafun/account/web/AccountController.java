package com.akkafun.account.web;

import com.akkafun.account.service.AccountService;
import com.akkafun.base.api.BooleanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.akkafun.account.api.AccountUrl.CHECK_ENOUGH_BALANCE_URL;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping(value = CHECK_ENOUGH_BALANCE_URL, method = RequestMethod.GET)
    public BooleanWrapper checkAccountBalanceEnough(@RequestParam("userId") Long userId,
                                                    @RequestParam("balance") Long balance) {
        boolean result = accountService.checkEnoughBalance(userId, balance);
        return new BooleanWrapper(result);
    }


}
