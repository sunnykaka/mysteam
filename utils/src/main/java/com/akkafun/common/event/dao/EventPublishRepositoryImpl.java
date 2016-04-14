package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.utils.SQLUtils;
import com.akkafun.common.utils.UpdateByIdFunction;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * Created by liubin on 2016/3/29.
 */
public class EventPublishRepositoryImpl implements EventPublishRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityManager getEm() {
        return em;
    }

    @Override
    public int updateStatusBatch(Long[] ids, EventPublishStatus status) {

        return SQLUtils.updateByIdBatch(ids, (updateIds) -> {
            StringBuilder sql = new StringBuilder(
                    String.format("update event_publish set status ='%s' where id in (", status.toString()));
            for (Long id : updateIds) {
                sql.append(id).append(",");
            }
            sql.replace(sql.length()-1, sql.length(), ")");
            return getEm().createNativeQuery(sql.toString()).executeUpdate();
        });
    }
}
