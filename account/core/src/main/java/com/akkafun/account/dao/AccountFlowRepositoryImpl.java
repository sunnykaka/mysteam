package com.akkafun.account.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by liubin on 2016/4/26.
 */
public class AccountFlowRepositoryImpl implements AccountFlowRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public EntityManager getEm() {
        return em;
    }
}
