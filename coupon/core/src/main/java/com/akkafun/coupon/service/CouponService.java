package com.akkafun.coupon.service;

import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.dao.CouponRepository;
import com.akkafun.coupon.domain.Coupon;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by liubin on 2016/4/29.
 */
@Service
public class CouponService {

    @Autowired
    CouponRepository couponRepository;

    @Transactional
    public Coupon initCoupon(Long userId) {
        Coupon coupon = new Coupon();
        coupon.setAmount(10000L);
        coupon.setCode(RandomStringUtils.randomAlphanumeric(8));
        coupon.setState(CouponState.VALID);
        coupon.setUserId(userId);

        couponRepository.save(coupon);
        return coupon;
    }

    @Transactional(readOnly = true)
    public boolean checkCoupon(Long couponId) {
        Coupon coupon = couponRepository.findOne(couponId);
        return coupon != null && coupon.getState().equals(CouponState.VALID);
    }
}
