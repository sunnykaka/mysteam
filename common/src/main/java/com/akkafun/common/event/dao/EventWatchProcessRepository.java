package com.akkafun.common.event.dao;

import com.akkafun.common.event.constant.ProcessStatus;
import com.akkafun.common.event.domain.EventWatchProcess;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by liubin on 2016/3/29.
 */
public interface EventWatchProcessRepository extends PagingAndSortingRepository<EventWatchProcess, Long>,
        EventWatchProcessRepositoryCustom{

    List<EventWatchProcess> findByStatus(ProcessStatus status);
}
