package com.akkafun.coupon.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by liubin on 2016/4/26.
 */
public class CouponRepositoryImpl implements CouponRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public EntityManager getEm() {
        return em;
    }
}
