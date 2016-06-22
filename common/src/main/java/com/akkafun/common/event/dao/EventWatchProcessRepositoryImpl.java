package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.ProcessStatus;
import com.akkafun.common.utils.SQLUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liubin on 2016/3/29.
 */
public class EventWatchProcessRepositoryImpl implements EventWatchProcessRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EntityManager getEm() {
        return em;
    }


    @Override
    public int updateStatusBatch(Long[] ids, ProcessStatus status) {

        return SQLUtils.updateByIdBatch(ids, (updateIds) -> {
            StringBuilder sql = new StringBuilder(
                    String.format("update event_watch_process set status ='%s' where id in (", status.toString()));
            for (Long id : updateIds) {
                sql.append(id).append(",");
            }
            sql.replace(sql.length()-1, sql.length(), ")");
            return getEm().createNativeQuery(sql.toString()).executeUpdate();
        });
    }


}
