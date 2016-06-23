package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.ProcessStatus;
import com.akkafun.common.event.domain.EventPublish;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by liubin on 2016/3/29.
 */
@NoRepositoryBean
public interface EventPublishRepository<T extends EventPublish> extends PagingAndSortingRepository<T, Long>{

    List<T> findByStatus(ProcessStatus status);

    T getByEventId(Long eventId);

    List<T> findByEventIdIn(List<Long> eventIds);

}
