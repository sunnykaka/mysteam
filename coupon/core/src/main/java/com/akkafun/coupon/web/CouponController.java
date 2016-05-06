package com.akkafun.coupon.web;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.akkafun.coupon.api.CouponUrl.CHECK_VALID_URL;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
public class CouponController {

    @Autowired
    CouponService couponService;

    @RequestMapping(value = CHECK_VALID_URL, method = RequestMethod.GET)
    public BooleanWrapper checkCoupon(@PathVariable("couponId") Long couponId) {
        boolean valid = couponService.checkCoupon(couponId);
        return new BooleanWrapper(valid);
    }


}
