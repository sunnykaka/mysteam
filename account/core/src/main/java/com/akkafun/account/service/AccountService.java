package com.akkafun.account.service;

import com.akkafun.account.dao.AccountFlowRepository;
import com.akkafun.account.dao.AccountRepository;
import com.akkafun.account.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by liubin on 2016/4/26.
 */
@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountFlowRepository accountFlowRepository;

    @Transactional
    public Account initAccount(Long userId) {
        Account account = new Account();
        account.setBalance(0L);
        account.setUserId(userId);
        accountRepository.save(account);

        return account;
    }

    @Transactional(readOnly = true)
    public boolean checkEnoughBalance(Long userId, Long balance) {
        Account account = accountRepository.findByUserId(userId);
        return account != null && account.getBalance() >= balance;
    }
}
