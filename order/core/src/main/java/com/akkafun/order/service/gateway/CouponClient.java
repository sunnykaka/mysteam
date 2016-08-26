package com.akkafun.order.service.gateway;

import com.akkafun.coupon.api.CouponUrl;
import com.akkafun.coupon.api.dtos.CouponDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by liubin on 2016/8/25.
 */
@FeignClient(CouponUrl.SERVICE_HOSTNAME)
public interface CouponClient {

    @RequestMapping(method = RequestMethod.GET, value = CouponUrl.COUPON_LIST_URL)
    List<CouponDto> findCoupons(@RequestParam("id") List<Long> id);

}
