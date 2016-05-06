package com.akkafun.order.service;

import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.dao.OrderCouponRepository;
import com.akkafun.order.dao.OrderItemRepository;
import com.akkafun.order.dao.OrderRepository;
import com.akkafun.order.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by liubin on 2016/4/29.
 */
@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderCouponRepository orderCouponRepository;

    /**
     * 下订单
     * @param placeOrderDto
     * @return
     */
    @Transactional
    public Order placeOrder(PlaceOrderDto placeOrderDto) {
        return null;
    }
}
