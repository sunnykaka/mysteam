package com.akkafun.coupon.web;

import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.coupon.domain.Coupon;
import com.akkafun.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.akkafun.coupon.api.CouponUrl.CHECK_VALID_URL;
import static com.akkafun.coupon.api.CouponUrl.COUPON_LIST_URL;
import static com.akkafun.coupon.api.CouponUrl.USER_COUPON_LIST_URL;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CouponController {

    @Autowired
    CouponService couponService;

    @RequestMapping(value = CHECK_VALID_URL, method = RequestMethod.GET)
    public BooleanWrapper checkCoupon(@PathVariable("couponId") Long couponId) {
        boolean valid = couponService.checkCoupon(couponId);
        return new BooleanWrapper(valid);
    }

    @RequestMapping(value = COUPON_LIST_URL, method = RequestMethod.GET)
    public List<CouponDto> listCouponsById(@RequestParam("id") Long[] ids) {

        List<Long> idList = Arrays.asList(ids);

        List<Coupon> coupons = couponService.findCouponsById(idList);

        return coupons.stream().map(this::convertCouponDto).collect(Collectors.toList());

    }


    @RequestMapping(value = USER_COUPON_LIST_URL, method = RequestMethod.GET)
    public List<CouponDto> listUserCoupons(@PathVariable("userId") Long userId) {

        List<Coupon> coupons = couponService.findCouponsByUser(userId);
        return coupons.stream().map(this::convertCouponDto).collect(Collectors.toList());

    }

    private CouponDto convertCouponDto(Coupon coupon) {
        CouponDto couponDto = new CouponDto();
        couponDto.setAmount(coupon.getAmount());
        couponDto.setCode(coupon.getCode());
        couponDto.setCreateTime(coupon.getCreateTime());
        couponDto.setId(coupon.getId());
        couponDto.setOrderId(coupon.getOrderId());
        couponDto.setState(coupon.getState());
        couponDto.setUpdateTime(coupon.getUpdateTime());
        couponDto.setUserId(coupon.getUserId());
        couponDto.setUseTime(coupon.getUseTime());
        return couponDto;
    }


}
