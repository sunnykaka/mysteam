package com.akkafun.common.event.domain;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.domain.VersionEntity;
import com.akkafun.common.event.constant.EventCategory;
import com.akkafun.common.event.constant.EventPublishStatus;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="event_category")
@Table(name = "event_publish")
public abstract class EventPublish extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    @Enumerated(EnumType.STRING)
    private EventPublishStatus status = EventPublishStatus.NEW;

    @Column
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;

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

    public EventPublishStatus getStatus() {
        return status;
    }

    public void setStatus(EventPublishStatus status) {
        this.status = status;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
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
