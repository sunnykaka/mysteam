package com.akkafun.common.event.domain;

import com.akkafun.common.domain.AuditableEntity;
import com.akkafun.common.event.constant.EventPublishStatus;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "event_publish")
public class EventPublish extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    @Enumerated(EnumType.STRING)
    private EventPublishStatus status = EventPublishStatus.NEW;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public EventPublishStatus getStatus() {
        return status;
    }

    public void setStatus(EventPublishStatus status) {
        this.status = status;
    }
}
