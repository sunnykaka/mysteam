package com.akkafun.coupon.dao;

import com.akkafun.coupon.domain.Coupon;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by liubin on 2016/4/26.
 */
public interface CouponRepository extends PagingAndSortingRepository<Coupon, Long>, CouponRepositoryCustom {

    List<Coupon> findByUserId(Long userId);

}
