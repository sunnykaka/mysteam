package com.akkafun.order.dao;

import com.akkafun.order.domain.Order;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/4/26.
 */
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>, OrderRepositoryCustom {
}
