package com.akkafun.common.event.dao;

import com.akkafun.common.dao.AbstractRepository;
import com.akkafun.common.event.constant.ProcessStatus;

/**
 * Created by liubin on 2016/3/29.
 */
public interface EventWatchProcessRepositoryCustom extends AbstractRepository{

    int updateStatusBatch(Long[] ids, ProcessStatus status);

}
