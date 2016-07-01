package com.akkafun.account.web;

import com.akkafun.account.domain.Account;
import com.akkafun.account.service.AccountService;
import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.akkafun.account.api.AccountUrl.ACCOUNT_BALANCE;
import static com.akkafun.account.api.AccountUrl.ACCOUNT_TRANSACTIONS;
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
    public BooleanWrapper checkAccountBalanceEnough(@PathVariable("userId") Long userId,
                                                    @RequestParam("balance") Long balance) {
        boolean result = accountService.checkEnoughBalance(userId, balance);
        return new BooleanWrapper(result);
    }

    @RequestMapping(value = ACCOUNT_BALANCE, method = RequestMethod.GET)
    public Long accountBalance(@PathVariable(value = "userId") Long userId) {

        return accountService.getByUserId(userId).getBalance();
    }

    @RequestMapping(value = ACCOUNT_TRANSACTIONS, method = RequestMethod.POST)
    public Long operateAccountBalance(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "amount") Long amount) {

        if(amount >= 0L) {
            return accountService.addBalance(userId, amount);
        } else {
            return accountService.reduceBalance(userId, Math.abs(amount));
        }
    }

}
