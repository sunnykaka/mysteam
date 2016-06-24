package com.akkafun.order.web;

import com.akkafun.order.api.dtos.OrderDto;
import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.domain.Order;
import com.akkafun.order.service.OrderService;
import com.akkafun.order.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.akkafun.order.api.OrderUrl.ORDER_INFO;
import static com.akkafun.order.api.OrderUrl.PLACE_ORDER;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    @Autowired
    OrderService orderService;

    @RequestMapping(value = PLACE_ORDER, method = RequestMethod.POST)
    public OrderDto placeOrder(@Valid @RequestBody PlaceOrderDto placeOrderDto) {

        Order order = orderService.placeOrder(placeOrderDto);

        return OrderUtils.convertToDto(order);
    }

    @RequestMapping(value = ORDER_INFO, method = RequestMethod.GET)
    public OrderDto orderInfo(@PathVariable("orderId") Long orderId) {

        Order order = orderService.get(orderId);

        return OrderUtils.convertToDto(order);
    }



}
