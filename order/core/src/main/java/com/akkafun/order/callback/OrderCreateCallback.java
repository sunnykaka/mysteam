package com.akkafun.order.callback;

import com.akkafun.account.api.events.AskReduceBalance;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.coupon.api.events.AskUseCoupon;
import com.akkafun.order.service.OrderService;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Created by liubin on 2016/6/16.
 */
public class OrderCreateCallback {

    public void onSuccess(AskReduceBalance askReduceBalance, AskUseCoupon askUseCoupon, String orderId) {

        long oId = NumberUtils.toLong(orderId);
        if(oId == 0L) {
            throw new AppBusinessException("orderId为空: " + orderId);
        }

        OrderService orderService = ApplicationContextHolder.context.getBean(OrderService.class);

        orderService.markCreateSuccess(oId);
    }

    public void onFailure(AskReduceBalance askReduceBalance, AskUseCoupon askUseCoupon,
                          String orderId, FailureInfo failureInfo) {

        long oId = NumberUtils.toLong(orderId);
        if(oId == 0L) {
            throw new AppBusinessException("orderId为空: " + orderId);
        }

        OrderService orderService = ApplicationContextHolder.context.getBean(OrderService.class);

        orderService.markCreateFail(oId);

    }

}
