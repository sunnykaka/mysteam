package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.EventProcessStatus;
import com.akkafun.common.utils.SQLUtils;

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

    @Override
    public int updateStatusToProcessed(Long eventProcessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", eventProcessId);
        params.put("status", EventProcessStatus.PROCESSED);
        params.put("oldStatus", EventProcessStatus.NEW);

        return update("update EventProcess ep set ep.status = :status where ep.id = :id and ep.status = :oldStatus", params);
    }
}
