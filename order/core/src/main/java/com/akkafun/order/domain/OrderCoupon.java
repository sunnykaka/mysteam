package com.akkafun.order.domain;

import javax.persistence.*;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "order_coupon")
public class OrderCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long couponId;

    @Column
    private Long couponAmount;

    @Column
    private String couponCode;

    @Column
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(Long couponAmount) {
        this.couponAmount = couponAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
