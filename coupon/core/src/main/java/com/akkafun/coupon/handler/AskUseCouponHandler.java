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
        CouponService couponService = ApplicationContextHolder.context.getBean(CouponService.class);
        try {
            couponService.revokeUse(originEvent.getCouponIds(), originEvent.getUserId(), originEvent.getOrderId());
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public BooleanWrapper processRequest(AskUseCoupon event) {
        if(event.getCouponIds() == null || event.getCouponIds().isEmpty()
                || event.getUserId() == null || event.getOrderId() == null) {
            return new BooleanWrapper(false, "couponId or userId or orderId is null");
        }

        CouponService couponService = ApplicationContextHolder.context.getBean(CouponService.class);
        try {
            couponService.useCoupon(event.getCouponIds(), event.getUserId(), event.getOrderId());
            return new BooleanWrapper(true);
        } catch (AppBusinessException e) {
            return new BooleanWrapper(false, e.getMessage());
        } catch (Exception e) {
            logger.error("", e);
            return new BooleanWrapper(false, e.getMessage());
        }

    }
}
