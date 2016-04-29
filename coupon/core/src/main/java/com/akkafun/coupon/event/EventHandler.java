package com.akkafun.coupon.event;

import com.akkafun.common.event.Subscribe;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.coupon.service.CouponService;
import com.akkafun.user.api.events.UserCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liubin on 2016/4/14.
 */
public class EventHandler {

    protected Logger logger = LoggerFactory.getLogger(EventHandler.class);

    CouponService couponService = ApplicationContextHolder.context.getBean(CouponService.class);

    @Subscribe
    public void handleUserCreatedEvent(UserCreated event) {
        couponService.initCoupon(event.getUserId());
    }

}
