package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.ProcessStatus;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liubin on 2016/3/29.
 */
public class EventProcessRepositoryImpl implements EventProcessRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityManager getEm() {
        return em;
    }

}
