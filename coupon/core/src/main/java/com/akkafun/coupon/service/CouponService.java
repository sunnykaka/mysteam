package com.akkafun.coupon.service;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.coupon.dao.CouponRepository;
import com.akkafun.coupon.domain.Coupon;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    /**
     * 查询优惠券列表, 如果对应id在数据库不存在, 返回的List的位置对应null值.
     * 入参数组长度一定等于返回的List长度.
     *
     * @param ids
     * @return
     */
    public List<CouponDto> findCoupons(Long[] ids) {
        if(ids == null || ids.length == 0) return new ArrayList<>();
        if(ids.length > 50) throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "一次查询的id数量不能超过50");

        List<Long> idList = Arrays.asList(ids);
        Iterable<Coupon> coupons = couponRepository.findAll(idList);

        Map<Long, CouponDto> couponDtoMap = StreamSupport.stream(coupons.spliterator(), false)
                .map(coupon -> {
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
                }).collect(Collectors.toMap(CouponDto::getId, Function.identity()));

        return idList.stream().map(couponDtoMap::get).collect(Collectors.toList());
    }
}
