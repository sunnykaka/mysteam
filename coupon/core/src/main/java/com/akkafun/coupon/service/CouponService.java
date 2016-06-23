package com.akkafun.coupon.service;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.utils.CustomPreconditions;
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
     * @param idList
     * @return
     *
     */
    public List<Coupon> findCouponsById(List<Long> idList) {
        if(idList == null || idList.isEmpty()) return new ArrayList<>();
        CustomPreconditions.assertNotGreaterThanMaxQueryBatchSize(idList.size());

        Map<Long, Coupon> couponMap = StreamSupport
                .stream(couponRepository.findAll(idList).spliterator(), false)
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));

        return idList.stream().map(couponMap::get).collect(Collectors.toList());

    }


    public List<Coupon> findCouponsByUser(Long userId) {
        if(userId == null) return new ArrayList<>();
        return couponRepository.findByUserId(userId);

    }
}
