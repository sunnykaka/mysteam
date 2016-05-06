package com.akkafun.order.web;

import com.akkafun.order.api.dtos.OrderDto;
import com.akkafun.order.api.dtos.OrderItemDto;
import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.domain.Order;
import com.akkafun.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static com.akkafun.order.api.OrderUrl.PLACE_ORDER;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @RequestMapping(value = PLACE_ORDER, method = RequestMethod.POST)
    public OrderDto placeOrder(@Valid @RequestBody PlaceOrderDto placeOrderDto) {

        Order order = orderService.placeOrder(placeOrderDto);

        OrderDto orderDto = new OrderDto();
        orderDto.setCreateTime(order.getCreateTime());
        orderDto.setId(order.getId());
        orderDto.setOrderNo(order.getOrderNo());
        orderDto.setPayAmount(order.getPayAmount());
        orderDto.setStatus(order.getStatus());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setUpdateTime(order.getUpdateTime());
        orderDto.setUserId(order.getUserId());
        orderDto.setOrderItemList(order.getOrderItemList().stream().map(orderItem -> {
            OrderItemDto orderItemDto = new OrderItemDto();
            orderItemDto.setId(orderItem.getId());
            orderItemDto.setPrice(orderItem.getPrice());
            orderItemDto.setProductId(orderItem.getProductId());
            orderItemDto.setQuantity(orderItem.getQuantity());
            return orderItemDto;
        }).collect(Collectors.toList()));

        return orderDto;
    }


}
