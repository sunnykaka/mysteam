package com.akkafun.account.service;

import com.akkafun.account.dao.AccountFlowRepository;
import com.akkafun.account.dao.AccountRepository;
import com.akkafun.account.domain.Account;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
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

    @Transactional
    public Long reduceBalance(Long userId, Long balance) {
        Account account = accountRepository.findByUserId(userId);
        if(account == null) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据userId找不到account, userId: " + userId);
        }
        if(account.getBalance() - balance < 0L) {
            throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "账户余额不足");
        }
        if(!balance.equals(0L)) {
            account.setBalance(account.getBalance() - balance);
            accountRepository.save(account);
        }
        return account.getBalance();
    }

    @Transactional
    public Long addBalance(Long userId, Long balance) {
        Account account = accountRepository.findByUserId(userId);
        if(account == null) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据userId找不到account, userId: " + userId);
        }
        if(!balance.equals(0L)) {
            account.setBalance(account.getBalance() + balance);
            accountRepository.save(account);
        }
        return account.getBalance();
    }


    @Transactional(readOnly = true)
    public Account get(Long accountId) {
        Account account = accountRepository.findOne(accountId);
        if(account == null) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据id找不到account, id: " + accountId);
        }
        return account;
    }

    @Transactional(readOnly = true)
    public Account getByUserId(Long userId) {
        Account account = accountRepository.findByUserId(userId);
        if(account == null) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据userId找不到account, userId: " + userId);
        }
        return account;
    }


}
