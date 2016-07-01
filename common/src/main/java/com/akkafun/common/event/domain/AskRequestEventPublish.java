package com.akkafun.common.event.domain;

import com.akkafun.common.event.constant.AskEventStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@DiscriminatorValue("ASK")
public class AskRequestEventPublish extends EventPublish {

    @Column
    @Enumerated(EnumType.STRING)
    private AskEventStatus askEventStatus;

    @Column
    private Long watchId;

    public AskEventStatus getAskEventStatus() {
        return askEventStatus;
    }

    public void setAskEventStatus(AskEventStatus askEventStatus) {
        this.askEventStatus = askEventStatus;
    }

    public Long getWatchId() {
        return watchId;
    }

    public void setWatchId(Long watchId) {
        this.watchId = watchId;
    }

}
