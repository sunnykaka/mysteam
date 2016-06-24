package com.akkafun.order.utils;

import com.akkafun.order.api.dtos.OrderDto;
import com.akkafun.order.api.dtos.OrderItemDto;
import com.akkafun.order.domain.Order;

import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/6/24.
 */
public class OrderUtils {


    public static OrderDto convertToDto(Order order) {

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
