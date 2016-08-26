package com.akkafun.order.service.gateway;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.base.exception.RemoteCallException;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/7/18.
 */
@Service
public class CouponGateway {

    protected Logger logger = LoggerFactory.getLogger(CouponGateway.class);

    @Autowired
    CouponClient couponClient;

    @HystrixCommand(ignoreExceptions = RemoteCallException.class)
    public List<CouponDto> findCoupons(List<Long> couponIds) {

        if(couponIds.isEmpty()) return new ArrayList<>();

        List<CouponDto> couponDtoList = couponClient.findCoupons(couponIds);

        if(!couponDtoList.isEmpty()) {

            //过滤出无效的优惠券
            List<CouponDto> notValidCouponDtoList = couponDtoList.stream()
                    .filter(couponDto -> !couponDto.getState().equals(CouponState.VALID))
                    .collect(Collectors.toList());
            if (!notValidCouponDtoList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.NOT_FOUND,
                        String.format("无效的优惠券信息, 优惠券id: %s",
                                notValidCouponDtoList.stream().map(CouponDto::getId).collect(Collectors.toList())));
            }
        }

        return couponDtoList;
    }



}
