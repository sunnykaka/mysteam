package com.akkafun.common.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.domain.VersionEntity;
import com.akkafun.common.event.constant.EventCategory;
import com.akkafun.common.event.constant.ProcessStatus;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "event_process")
public class EventProcess extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    @Enumerated(EnumType.STRING)
    private ProcessStatus status = ProcessStatus.NEW;

    @Column
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;


    @Column(unique = true)
    private Long eventId;

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

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "EventProcess{" +
                "id=" + id +
                ", payload='" + payload + '\'' +
                ", status=" + status +
                ", eventCategory=" + eventCategory +
                ", eventId=" + eventId +
                ", eventType=" + eventType +
                "} " + super.toString();
    }
}
