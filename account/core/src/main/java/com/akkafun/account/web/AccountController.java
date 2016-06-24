package com.akkafun.account.web;

import com.akkafun.account.domain.Account;
import com.akkafun.account.service.AccountService;
import com.akkafun.base.api.BooleanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.akkafun.account.api.AccountUrl.ACCOUNT_BALANCE;
import static com.akkafun.account.api.AccountUrl.CHECK_ENOUGH_BALANCE;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping(value = CHECK_ENOUGH_BALANCE, method = RequestMethod.GET)
    public BooleanWrapper checkAccountBalanceEnough(@RequestParam("userId") Long userId,
                                                    @RequestParam("balance") Long balance) {
        boolean result = accountService.checkEnoughBalance(userId, balance);
        return new BooleanWrapper(result);
    }

    @RequestMapping(value = ACCOUNT_BALANCE, method = RequestMethod.GET)
    public Long accountBalance(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "userId", required = false) Long userId) {
        Long balance = 0L;
        if(accountId != null) {
            balance = accountService.get(accountId).getBalance();
        } else if(userId != null) {
            balance = accountService.getByUserId(userId).getBalance();
        }
        return balance;
    }


}
