package com.akkafun.coupon.api.events;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.base.event.domain.Revokable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by liubin on 2016/4/8.
 */
public class AskUseCoupon extends AskEvent implements Revokable {

    public static final EventType EVENT_TYPE = EventType.ASK_USE_COUPON;

    @Override
    public EventType getType() {
        return EVENT_TYPE;
    }

    private List<Long> couponIds;

    private Long userId;

    private Long orderId;

    @JsonCreator
    public AskUseCoupon(
            @JsonProperty("couponIds") List<Long> couponIds,
            @JsonProperty("userId") Long userId,
            @JsonProperty("orderId") Long orderId) {
        this.couponIds = couponIds;
        this.userId = userId;
        this.orderId = orderId;
    }

    public List<Long> getCouponIds() {
        return couponIds;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "AskUseCoupon{" +
                "couponIds=" + couponIds +
                ", userId=" + userId +
                ", orderId=" + orderId +
                "} " + super.toString();
    }
}
