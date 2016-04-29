package com.akkafun.coupon.domain;

import com.akkafun.common.domain.VersionEntity;
import com.akkafun.coupon.api.constants.CouponState;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "coupon")
public class Coupon extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long amount;

    @Column
    private Long userId;

    @Column
    @Enumerated(value = EnumType.STRING)
    private CouponState state;

    @Column
    private Long orderId;

    @Column
    private LocalDateTime useTime;

    @Column
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CouponState getState() {
        return state;
    }

    public void setState(CouponState state) {
        this.state = state;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getUseTime() {
        return useTime;
    }

    public void setUseTime(LocalDateTime useTime) {
        this.useTime = useTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
