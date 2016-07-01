package com.akkafun.common.event.domain;

import com.akkafun.common.domain.VersionEntity;
import com.akkafun.common.event.constant.AskEventStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "event_watch")
public class EventWatch extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private AskEventStatus askEventStatus;

    @Column
    private String extraParams;

    @Column
    private String askEventIds;

    @Column
    private String callbackClass;

    @Column
    private boolean united;

    @Column
    private LocalDateTime timeoutTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AskEventStatus getAskEventStatus() {
        return askEventStatus;
    }

    public void setAskEventStatus(AskEventStatus askEventStatus) {
        this.askEventStatus = askEventStatus;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    public List<Long> getAskEventIds() {
        return Arrays.asList(askEventIds.split(",")).stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public void setAskEventIds(List<Long> askEventIds) {
        this.askEventIds = String.join(",",
                askEventIds.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public boolean isUnited() {
        return united;
    }

    public void setUnited(boolean united) {
        this.united = united;
    }

    public LocalDateTime getTimeoutTime() {
        return timeoutTime;
    }

    public void setTimeoutTime(LocalDateTime timeoutTime) {
        this.timeoutTime = timeoutTime;
    }
}
