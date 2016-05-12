package com.akkafun.order.service;

import com.akkafun.account.api.AccountUrl;
import com.akkafun.base.api.BooleanWrapper;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.common.event.service.EventBus;
import com.akkafun.coupon.api.CouponUrl;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.order.api.constants.OrderStatus;
import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.api.events.OrderCreatePending;
import com.akkafun.order.dao.OrderCouponRepository;
import com.akkafun.order.dao.OrderItemRepository;
import com.akkafun.order.dao.OrderRepository;
import com.akkafun.order.dao.ProductRepository;
import com.akkafun.order.domain.Order;
import com.akkafun.order.domain.OrderCoupon;
import com.akkafun.order.domain.OrderItem;
import com.akkafun.order.domain.Product;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    ProductRepository productRepository;

    @Autowired
    RestTemplate restTemplate;

    /**
     * The RestTemplate works because it uses a custom request-factory that uses
     * Ribbon to look-up the service to use. This method simply exists to show
     * this.
     */
    @PostConstruct
    public void demoOnly() {
        // Can't do this in the constructor because the RestTemplate injection
        // happens afterwards.
        logger.warn("The RestTemplate request factory is "
                + restTemplate.getRequestFactory());
    }


    /**
     * 下订单
     * @param placeOrderDto
     * @return
     */
    @Transactional
    public Order placeOrder(PlaceOrderDto placeOrderDto) {
        Order order = new Order();
        order.setUserId(placeOrderDto.getUserId());
        order.setStatus(OrderStatus.CREATE_PENDING);
        order.setOrderNo(Long.parseLong(RandomStringUtils.randomNumeric(8)));
        List<OrderItem> orderItemList = placeOrderDto.getPlaceOrderItemList().stream().map(placeOrderItemDto -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(placeOrderItemDto.getProductId());
            orderItem.setQuantity(placeOrderItemDto.getQuantity());
            Product product = productRepository.findOne(placeOrderItemDto.getProductId());
            if (product == null) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        "产品不存在, ID:" + placeOrderItemDto.getProductId());
            }
            orderItem.setPrice(product.getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        order.setTotalAmount(order.calcTotalAmount(orderItemList));

        List<OrderCoupon> orderCouponList = new ArrayList<>();
        Set<Long> couponIdSet = new HashSet<>(placeOrderDto.getCouponIdList());
        if(!couponIdSet.isEmpty()) {
            //处理优惠券信息

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(CouponUrl.buildUrl(CouponUrl.COUPON_LIST_URL))
                    .queryParam("id", couponIdSet.toArray())
                    .build().encode().toUri();

            ResponseEntity<List<CouponDto>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CouponDto>>() {
                    }
            );

            List<CouponDto> couponDtoList = response.getBody();
            if(couponDtoList == null || !response.getStatusCode().is2xxSuccessful()) {
                throw new AppBusinessException(
                        String.format("请求获取优惠券接口失败, 返回status: %s, 请求uri: %s",
                                response.getStatusCode().value(), uri.toString()));
            }

            List<Long> couponDtoIdList = couponDtoList.stream()
                    .map(CouponDto::getId)
                    .collect(Collectors.toList());
            //过滤出在数据库不存在的优惠券id列表
            List<Long> notExistIdList = couponIdSet.stream()
                    .filter(couponId -> !couponDtoIdList.contains(couponId))
                    .collect(Collectors.toList());
            if(!notExistIdList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        String.format("不存在的优惠券id: %s", notExistIdList.toString()));
            }

            //过滤出无效的优惠券
            List<CouponDto> notValidCouponDtoList = couponDtoList.stream()
                    .filter(couponDto -> !couponDto.getState().equals(CouponState.VALID))
                    .collect(Collectors.toList());
            if(!notValidCouponDtoList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        String.format("无效的优惠券信息, 优惠券id: %s",
                                notValidCouponDtoList.stream().map(CouponDto::getId).collect(Collectors.toList())));
            }

            orderCouponList = couponDtoList.stream().map(couponDto -> {
                OrderCoupon orderCoupon = new OrderCoupon();
                orderCoupon.setCouponAmount(couponDto.getAmount());
                orderCoupon.setCouponCode(couponDto.getCode());
                orderCoupon.setCouponId(couponDto.getId());
                return orderCoupon;
            }).collect(Collectors.toList());

        }

        long couponAmount = orderCouponList.stream().mapToLong(OrderCoupon::getCouponAmount).sum();
        order.setPayAmount(order.calcPayAmount(order.getTotalAmount(), couponAmount));

        //检验账户余额是否足够
        if(order.getPayAmount() > 0L) {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(AccountUrl.buildUrl(AccountUrl.CHECK_ENOUGH_BALANCE_URL))
                    .queryParam("userId", placeOrderDto.getUserId())
                    .queryParam("balance", order.getPayAmount())
                    .build().encode().toUri();

            BooleanWrapper booleanWrapper = restTemplate.getForObject(uri, BooleanWrapper.class);
            if(!booleanWrapper.isResult()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "下单失败, 账户余额不足");
            }
        }

        orderRepository.save(order);
        orderItemList.forEach(orderItem -> {
            orderItem.setOrderId(order.getId());
            orderItemRepository.save(orderItem);
        });
        orderCouponList.forEach(orderCoupon -> {
            orderCoupon.setOrderId(order.getId());
            orderCouponRepository.save(orderCoupon);
        });

        eventBus.publish(new OrderCreatePending(order.getId(), order.getOrderNo(), order.getTotalAmount(),
                order.getPayAmount(), order.getUserId()));

        return order;
    }
}
