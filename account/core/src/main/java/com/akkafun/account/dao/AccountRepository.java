package com.akkafun.account.dao;

import com.akkafun.account.domain.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/4/26.
 */
public interface AccountRepository extends PagingAndSortingRepository<Account, Long>, AccountRepositoryCustom {

    Account findByUserId(Long userId);

}
