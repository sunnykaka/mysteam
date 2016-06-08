package com.akkafun.common.event.dao;

import com.akkafun.common.event.domain.EventWatch;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/3/29.
 */
public interface EventWatchRepository extends PagingAndSortingRepository<EventWatch, Long>, EventWatchRepositoryCustom{


}
