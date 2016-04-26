package com.akkafun.account.domain;

import com.akkafun.account.api.constants.AccountFlowType;
import com.akkafun.common.domain.AuditEntity;
import com.akkafun.common.domain.VersionEntity;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "account_flow")
public class AccountFlow extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long balance;

    @Column
    private Long accountId;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountFlowType type;

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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccountFlowType getType() {
        return type;
    }

    public void setType(AccountFlowType type) {
        this.type = type;
    }
}
