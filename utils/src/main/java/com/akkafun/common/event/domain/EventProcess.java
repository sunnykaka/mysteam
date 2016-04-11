package com.akkafun.common.event.domain;

import com.akkafun.common.domain.AuditableEntity;
import com.akkafun.common.event.constant.EventProcessStatus;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "event_process")
public class EventProcess extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    private String password;

    @Column
    @Enumerated(EnumType.STRING)
    private EventProcessStatus status;


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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EventProcessStatus getStatus() {
        return status;
    }

    public void setStatus(EventProcessStatus status) {
        this.status = status;
    }
}
