package com.akkafun.order.dao;

import com.akkafun.order.domain.OrderItem;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/4/26.
 */
public interface OrderItemRepository extends PagingAndSortingRepository<OrderItem, Long>{
}
