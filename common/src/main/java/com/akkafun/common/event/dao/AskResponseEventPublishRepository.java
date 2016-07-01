package com.akkafun.common.event.dao;

import com.akkafun.common.event.domain.AskResponseEventPublish;

/**
 * Created by liubin on 2016/3/29.
 */
public interface AskResponseEventPublishRepository extends
        EventPublishRepository<AskResponseEventPublish>, AskResponseEventPublishRepositoryCustom {

    Long countByAskEventId(Long askEventId);

    AskResponseEventPublish getByAskEventId(Long askEventId);
}
