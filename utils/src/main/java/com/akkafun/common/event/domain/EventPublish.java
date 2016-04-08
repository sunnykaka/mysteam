package com.akkafun.common.event.domain;

import com.akkafun.common.domain.AuditableEntity;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
public class EventPublish extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String payload;

    @Column
    private String password;


}
