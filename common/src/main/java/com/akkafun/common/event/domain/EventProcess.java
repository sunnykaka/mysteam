package com.akkafun.common.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.domain.AuditEntity;
import com.akkafun.common.event.constant.EventProcessStatus;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "event_process")
public class EventProcess extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    @Enumerated(EnumType.STRING)
    private EventProcessStatus status = EventProcessStatus.NEW;

    @Column(unique = true)
    private String eventId;

    @Column
    @Enumerated(EnumType.STRING)
    private EventType eventType;


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

    public EventProcessStatus getStatus() {
        return status;
    }

    public void setStatus(EventProcessStatus status) {
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
