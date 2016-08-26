package com.akkafun.order.service;

import com.akkafun.account.api.events.AskReduceBalance;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.event.AskParameterBuilder;
import com.akkafun.common.event.service.EventBus;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.coupon.api.events.AskUseCoupon;
import com.akkafun.order.api.constants.OrderStatus;
import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.api.dtos.PlaceOrderItemDto;
import com.akkafun.order.callback.OrderCreateCallback;
import com.akkafun.order.dao.OrderCouponRepository;
import com.akkafun.order.dao.OrderItemRepository;
import com.akkafun.order.dao.OrderRepository;
import com.akkafun.order.domain.Order;
import com.akkafun.order.domain.OrderCoupon;
import com.akkafun.order.domain.OrderItem;
import com.akkafun.order.service.gateway.*;
import com.akkafun.product.api.dtos.ProductDto;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/4/29.
 */
@Service
public class OrderService {

    protected Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    EventBus eventBus;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderCouponRepository orderCouponRepository;

    @Autowired
    CouponGateway couponGateway;

    @Autowired
    ProductGateway productGateway;

    @Autowired
    AccountGateway accountGateway;


    @Transactional(readOnly = true)
    public Order get(Long orderId) {
        Order order = null;
        if(orderId != null) {
            order = orderRepository.findOne(orderId);
        }
        if(order == null) {
            throw new AppBusinessException(CommonErrorCode.NOT_FOUND, "根据id找不到订单, id: " + orderId);
        }
        return order;
    }

    /**
     * 下订单
     *
     * @param placeOrderDto
     * @return
     */
    @Transactional
    public Order placeOrder(PlaceOrderDto placeOrderDto) {
        Order order = new Order();
        order.setUserId(placeOrderDto.getUserId());
        order.setStatus(OrderStatus.CREATE_PENDING);
        order.setOrderNo(Long.parseLong(RandomStringUtils.randomNumeric(8)));

        //查询产品信息
        List<Long> productIds = placeOrderDto.getPlaceOrderItemList().stream()
                .map(PlaceOrderItemDto::getProductId)
                .collect(Collectors.toList());

        List<ProductDto> productDtoList = productGateway.findProducts(productIds);
        Map<Long, ProductDto> productDtoMap = productDtoList.stream()
                .collect(Collectors.toMap(ProductDto::getId, Function.identity()));

        List<OrderItem> orderItemList = placeOrderDto.getPlaceOrderItemList().stream().map(placeOrderItemDto -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(placeOrderItemDto.getProductId());
            orderItem.setQuantity(placeOrderItemDto.getQuantity());
            ProductDto productDto = productDtoMap.get(placeOrderItemDto.getProductId());
            orderItem.setPrice(productDto.getPrice());
            return orderItem;
        }).collect(Collectors.toList());
        order.setOrderItemList(orderItemList);

        order.setTotalAmount(order.calcTotalAmount());

        //查询优惠券信息
        List<OrderCoupon> orderCouponList = new ArrayList<>();
        Set<Long> couponIdSet = new HashSet<>(placeOrderDto.getCouponIdList());
        if (!couponIdSet.isEmpty()) {

            List<CouponDto> couponDtoList = couponGateway.findCoupons(new ArrayList<>(couponIdSet));

            orderCouponList = couponDtoList.stream().map(couponDto -> {
                OrderCoupon orderCoupon = new OrderCoupon();
                orderCoupon.setCouponAmount(couponDto.getAmount());
                orderCoupon.setCouponCode(couponDto.getCode());
                orderCoupon.setCouponId(couponDto.getId());
                return orderCoupon;
            }).collect(Collectors.toList());

        }

        //计算订单金额
        long couponAmount = orderCouponList.stream().mapToLong(OrderCoupon::getCouponAmount).sum();
        order.setPayAmount(order.calcPayAmount(order.getTotalAmount(), couponAmount));

        //检验账户余额是否足够
        if (order.getPayAmount() > 0L) {

            boolean balanceEnough = accountGateway.isBalanceEnough(placeOrderDto.getUserId(), order.getPayAmount());
            if(!balanceEnough) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "下单失败, 账户余额不足");
            }
        }

        //保存订单信息
        orderRepository.save(order);
        orderItemList.forEach(orderItem -> {
            orderItem.setOrderId(order.getId());
            orderItemRepository.save(orderItem);
        });
        orderCouponList.forEach(orderCoupon -> {
            orderCoupon.setOrderId(order.getId());
            orderCouponRepository.save(orderCoupon);
        });

        //解决订单金额为0还发送请求的问题
        Optional<AskReduceBalance> askReduceBalance = Optional.empty();
        if(order.getPayAmount() > 0L) {
            askReduceBalance = Optional.of(new AskReduceBalance(placeOrderDto.getUserId(), order.getPayAmount()));
        }
        Optional<AskUseCoupon> askUseCoupon = Optional.empty();
        if(!orderCouponList.isEmpty()) {
            List<Long> couponIds = orderCouponList.stream().map(OrderCoupon::getCouponId).collect(Collectors.toList());
            askUseCoupon = Optional.of(new AskUseCoupon(couponIds, placeOrderDto.getUserId(), order.getId()));
        }
        if(!askReduceBalance.isPresent() && !askUseCoupon.isPresent()) {
            markCreateSuccess(order.getId());

        } else {
            eventBus.ask(
                    AskParameterBuilder.askOptional(askReduceBalance, askUseCoupon)
                            .callbackClass(OrderCreateCallback.class)
                            .addParam("orderId", String.valueOf(order.getId()))
                            .build()
            );
        }

        return order;
    }


    @Transactional
    public void markCreateSuccess(Long orderId) {
        Order order = checkOrderBeforeMarkSuccessOrFail(orderId);
        order.setStatus(OrderStatus.CREATED);

        orderRepository.save(order);
    }

    @Transactional
    public void markCreateFail(Long orderId) {
        Order order = checkOrderBeforeMarkSuccessOrFail(orderId);
        order.setStatus(OrderStatus.CREATE_FAILED);

        orderRepository.save(order);
    }

    private Order checkOrderBeforeMarkSuccessOrFail(Long orderId) {
        Order order = get(orderId);
        if(!Objects.equals(order.getStatus(), OrderStatus.CREATE_PENDING)) {
            throw new AppBusinessException("订单状态不为CREATE_PENDING, id: " + orderId);
        }
        return order;
    }

}