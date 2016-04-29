package com.akkafun.order.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by liubin on 2016/4/26.
 */
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public EntityManager getEm() {
        return em;
    }
}
