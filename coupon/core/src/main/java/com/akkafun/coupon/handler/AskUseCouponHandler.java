package com.akkafun.coupon.handler;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.event.handler.RevokableAskEventHandler;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.akkafun.coupon.api.events.AskUseCoupon;
import com.akkafun.coupon.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liubin on 2016/6/24.
 */
public class AskUseCouponHandler implements RevokableAskEventHandler<AskUseCoupon> {

    private static Logger logger = LoggerFactory.getLogger(AskUseCouponHandler.class);


    @Override
    public void processRevoke(AskUseCoupon originEvent, FailureInfo failureInfo) {
        logger.debug("AskUseCouponHandler processRevoke, receive AskUseCoupon: " + originEvent);

        CouponService couponService = ApplicationContextHolder.context.getBean(CouponService.class);
        couponService.revokeUse(originEvent.getCouponIds(), originEvent.getUserId(), originEvent.getOrderId());
    }

    @Override
    public BooleanWrapper processRequest(AskUseCoupon event) {
        logger.debug("AskUseCouponHandler processRequest, receive AskUseCoupon: " + event);

        if(event.getCouponIds() == null || event.getCouponIds().isEmpty()
                || event.getUserId() == null || event.getOrderId() == null) {
            return new BooleanWrapper(false, "couponId or userId or orderId is null");
        }

        CouponService couponService = ApplicationContextHolder.context.getBean(CouponService.class);
        couponService.useCoupon(event.getCouponIds(), event.getUserId(), event.getOrderId());
        return new BooleanWrapper(true);

    }
}
