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
import com.akkafun.order.api.dtos.PlaceOrderItemDto;
import com.akkafun.order.api.events.OrderCreatePending;
import com.akkafun.order.dao.OrderCouponRepository;
import com.akkafun.order.dao.OrderItemRepository;
import com.akkafun.order.dao.OrderRepository;
import com.akkafun.order.domain.Order;
import com.akkafun.order.domain.OrderCoupon;
import com.akkafun.order.domain.OrderItem;
import com.akkafun.product.api.ProductUrl;
import com.akkafun.product.api.dtos.ProductDto;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    RestTemplate restTemplate;


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

        List<ProductDto> productDtoList = findProducts(productIds);
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

            List<CouponDto> couponDtoList = findCoupons(new ArrayList<>(couponIdSet));

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

            boolean balanceEnough = isBalanceEnough(placeOrderDto.getUserId(), order.getPayAmount());
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

        //发布事件
        eventBus.publish(new OrderCreatePending(order.getId(), order.getOrderNo(), order.getTotalAmount(),
                order.getPayAmount(), order.getUserId()));

        return order;
    }

    private List<ProductDto> findProducts(List<Long> productIds) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(ProductUrl.buildUrl(ProductUrl.PRODUCT_LIST_URL))
                .queryParam("id", productIds.toArray())
                .build().encode().toUri();

        ProductDto[] productDtos = restTemplate.getForObject(uri, ProductDto[].class);
        List<ProductDto> productDtoList = Arrays.asList(productDtos);

        if (!productDtoList.isEmpty()) {
            List<Long> productDtoIdList = productDtoList.stream()
                    .map(ProductDto::getId)
                    .collect(Collectors.toList());

            //过滤出根据接口查询不到的产品id列表
            List<Long> notExistIdList = productIds.stream()
                    .filter(productId -> !productDtoIdList.contains(productId))
                    .collect(Collectors.toList());

            if (!notExistIdList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        String.format("不存在的产品id: %s", notExistIdList.toString()));
            }
        }

        return productDtoList;
    }

    private List<CouponDto> findCoupons(List<Long> couponIds) {

        if(couponIds.isEmpty()) return new ArrayList<>();

        URI uri = UriComponentsBuilder
                .fromHttpUrl(CouponUrl.buildUrl(CouponUrl.COUPON_LIST_URL))
                .queryParam("id", couponIds.toArray())
                .build().encode().toUri();

        CouponDto[] couponDtos = restTemplate.getForObject(uri, CouponDto[].class);
        List<CouponDto> couponDtoList = Arrays.asList(couponDtos);

        if(!couponDtoList.isEmpty()) {
            List<Long> couponDtoIdList = couponDtoList.stream()
                    .map(CouponDto::getId)
                    .collect(Collectors.toList());
            //过滤出在数据库不存在的优惠券id列表
            List<Long> notExistIdList = couponIds.stream()
                    .filter(couponId -> !couponDtoIdList.contains(couponId))
                    .collect(Collectors.toList());
            if (!notExistIdList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        String.format("不存在的优惠券id: %s", notExistIdList.toString()));
            }

            //过滤出无效的优惠券
            List<CouponDto> notValidCouponDtoList = couponDtoList.stream()
                    .filter(couponDto -> !couponDto.getState().equals(CouponState.VALID))
                    .collect(Collectors.toList());
            if (!notValidCouponDtoList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.BAD_REQUEST,
                        String.format("无效的优惠券信息, 优惠券id: %s",
                                notValidCouponDtoList.stream().map(CouponDto::getId).collect(Collectors.toList())));
            }
        }

        return couponDtoList;
    }

    private boolean isBalanceEnough(Long userId, Long amount) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(AccountUrl.buildUrl(AccountUrl.CHECK_ENOUGH_BALANCE_URL))
                .queryParam("userId", userId)
                .queryParam("balance", amount)
                .build().encode().toUri();

        BooleanWrapper booleanWrapper = restTemplate.getForObject(uri, BooleanWrapper.class);

        return booleanWrapper.isResult();
    }

}