package com.akkafun.order.domain;

import com.akkafun.common.domain.VersionEntity;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.order.api.constants.OrderStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liubin on 2016/3/28.
 */
@Entity
@Table(name = "order_table")
public class Order extends VersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long orderNo;

    @Column
    private Long totalAmount;

    @Column
    private Long payAmount;

    @Column
    private Long userId;

    @Column
    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    private List<OrderItem> orderItemList = new ArrayList<>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    /**
     * 计算订单总额
     * @param
     */
    public Long calcTotalAmount() {
        return orderItemList.stream().mapToLong(orderItem -> orderItem.getPrice() * orderItem.getQuantity()).sum();
    }

    /**
     * 计算订单应付金额
     *
     * @param totalAmount
     * @param couponAmount
     * @return
     */
    public Long calcPayAmount(Long totalAmount, Long couponAmount) {
        return Math.max(totalAmount - couponAmount, 0L);
    }
}
