package com.akkafun.common.event.dao;

import com.akkafun.common.dao.AbstractRepository;
import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.domain.EventPublish;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by liubin on 2016/3/29.
 */
public interface EventPublishRepositoryCustom extends AbstractRepository{

    int updateStatusBatch(Long[] ids, EventPublishStatus status);

}
