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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by liubin on 2016/4/29.
 */
@Service
public class CouponService {

    private static Logger logger = LoggerFactory.getLogger(CouponService.class);

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
     * 查询优惠券列表, 如果对应id在数据库不存在, 返回404.
     * 入参数组长度一定等于返回的List长度.
     * @param idList
     * @return
     *
     */
    @Transactional(readOnly = true)
    public List<Coupon> findCouponsById(List<Long> idList) {
        if(idList == null || idList.isEmpty()) return new ArrayList<>();
        CustomPreconditions.assertNotGreaterThanMaxQueryBatchSize(idList.size());

        Map<Long, Coupon> couponMap = StreamSupport
                .stream(couponRepository.findAll(idList).spliterator(), false)
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));

        List<Long> notExistIdList = idList.stream().filter(
                id -> !couponMap.containsKey(id)).collect(Collectors.toList());

        //过滤出在数据库不存在的优惠券id列表
        if (!notExistIdList.isEmpty()) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND,
                    String.format("不存在的优惠券id: %s", notExistIdList.toString()));
        }

        return idList.stream().map(couponMap::get).collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public List<Coupon> findCouponsByUser(Long userId) {
        if(userId == null) return new ArrayList<>();
        return couponRepository.findByUserId(userId);

    }

    @Transactional
    public void useCoupon(List<Long> couponIds, Long userId, Long orderId) {
        List<Coupon> coupons = Lists.newArrayList(couponRepository.findAll(couponIds));
        if(coupons.isEmpty()) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据couponIds找到不到coupon, couponIds: " + couponIds);
        }
        coupons.forEach(coupon -> {
            if(!Objects.equals(coupon.getUserId(), userId)) {
                throw new AppBusinessException(CommonErrorCode.UNAUTHORIZED);
            }
            if(!coupon.getState().equals(CouponState.VALID)) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "优惠券已过期");
            }
        });
        coupons.forEach(coupon -> {
            coupon.setOrderId(orderId);
            coupon.setState(CouponState.USED);
            coupon.setUseTime(LocalDateTime.now());
            couponRepository.save(coupon);
        });


    }

    @Transactional
    public void revokeUse(List<Long> couponIds, Long userId, Long orderId) {
        List<Coupon> coupons = Lists.newArrayList(couponRepository.findAll(couponIds));
        if(coupons.isEmpty()) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据couponIds找到不到coupon, couponIds: " + couponIds);
        }
        coupons.forEach(coupon -> {
            if(!Objects.equals(coupon.getUserId(), userId) || !Objects.equals(coupon.getOrderId(), orderId)) {
                throw new AppBusinessException("userId或者orderId与coupon的属性不相等, couponId:" + coupon.getId());
            }
            if(!coupon.getState().equals(CouponState.USED)) {
                throw new AppBusinessException("优惠券状态不为已使用, couponId:" + coupon.getId());
            }
        });
        coupons.forEach(coupon -> {
            coupon.setOrderId(null);
            coupon.setState(CouponState.VALID);
            coupon.setUseTime(null);
            couponRepository.save(coupon);
        });
    }


}
