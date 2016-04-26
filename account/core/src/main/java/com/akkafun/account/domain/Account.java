package com.akkafun.account.domain;

import com.akkafun.common.domain.VersionEntity;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "account")
public class Account extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long balance;

    @Column
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
