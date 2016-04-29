package com.akkafun.order.service;

import com.akkafun.order.dao.OrderCouponRepository;
import com.akkafun.order.dao.OrderItemRepository;
import com.akkafun.order.dao.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
