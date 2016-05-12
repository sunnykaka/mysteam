package com.akkafun.coupon.web;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.akkafun.coupon.api.CouponUrl.CHECK_VALID_URL;
import static com.akkafun.coupon.api.CouponUrl.COUPON_LIST_URL;

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

    @RequestMapping(value = COUPON_LIST_URL, method = RequestMethod.GET)
    public List<CouponDto> couponList(@RequestParam("id") Long[] ids) {
        return couponService.findCoupons(ids);
    }


}
