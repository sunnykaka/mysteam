package com.akkafun.order.service;

import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.api.dtos.PlaceOrderItemDto;
import com.akkafun.order.domain.Order;
import com.akkafun.order.test.OrderBaseTest;
import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by liubin on 2016/5/9.
 */
public class OrderServiceTest extends OrderBaseTest {

    @Autowired
    OrderService orderService;

    @Test
    @Ignore
    public void test() {

        PlaceOrderDto placeOrderDto = new PlaceOrderDto();
        placeOrderDto.setUserId(1L);
        PlaceOrderItemDto placeOrderItemDto1 = new PlaceOrderItemDto();
        placeOrderItemDto1.setProductId(1L);
        placeOrderItemDto1.setQuantity(1);
        PlaceOrderItemDto placeOrderItemDto2 = new PlaceOrderItemDto();
        placeOrderItemDto2.setProductId(2L);
        placeOrderItemDto2.setQuantity(2);
        placeOrderDto.setPlaceOrderItemList(Lists.newArrayList(placeOrderItemDto1, placeOrderItemDto2));

        Order order = orderService.placeOrder(placeOrderDto);
        System.out.println(order.getId());

    }


}
