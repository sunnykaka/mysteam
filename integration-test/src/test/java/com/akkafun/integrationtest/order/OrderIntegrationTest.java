package com.akkafun.integrationtest.order;

import com.akkafun.account.api.AccountUrl;
import com.akkafun.coupon.api.CouponUrl;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.integrationtest.test.BaseIntegrationTest;
import com.akkafun.order.api.OrderUrl;
import com.akkafun.order.api.constants.OrderStatus;
import com.akkafun.order.api.dtos.OrderDto;
import com.akkafun.order.api.dtos.OrderItemDto;
import com.akkafun.order.api.dtos.PlaceOrderDto;
import com.akkafun.order.api.dtos.PlaceOrderItemDto;
import com.akkafun.product.api.ProductUrl;
import com.akkafun.product.api.dtos.ProductDto;
import com.akkafun.user.api.UserUrl;
import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.api.dtos.UserDto;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.akkafun.common.utils.TestUtils.createJsonEntity;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/3/29.
 */
public class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 不用优惠券, 用户初始金额100, 订单金额50
     */
    @Test
    public void testCreateOrderSuccess() {

        placeOrderSuccess(false, Optional.of(10000L), Optional.of(5000L));

    }

    /**
     * 不用优惠券, 用户初始金额100, 订单金额100
     */
    @Test
    public void testCreateOrderWithOrderAmount() {

        placeOrderSuccess(false, Optional.of(10000L), Optional.of(10000L));
    }

    /**
     * 不用优惠券, 用户初始金额100, 订单金额100
     */
    @Test
    public void testCreateOrderWithZeroInitBalance() {

        placeOrderSuccess(false, Optional.empty(), Optional.empty());
    }


    /**
     * 用优惠券, 用户初始金额100, 订单金额150
     */
    @Test
    public void testCreateOrderWithCouponSuccess() {

        placeOrderSuccess(true, Optional.of(10000L), Optional.of(15000L));
    }

    /**
     * 用优惠券, 优惠券金额100, 用户初始金额0, 订单金额100, 完全抵扣
     */
    @Test
    public void testCreateOrderWithCouponAndZeroInitBalance() {

        placeOrderSuccess(true, Optional.empty(), Optional.of(10000L));
    }

    /**
     * 用优惠券, 用户初始金额100, 订单金额150, 开始下单之后马上减掉用户100金额, 测试ask为false以及revoke是否能正常工作
     */
    @Test
    public void testCreateOrderWithDeficientAccountBalance() {

        UserDto userDto = initUser();

        initBalance(userDto.getId(), Optional.of(10000L));

        List<Long> couponForUse = initCoupon(userDto.getId(), true);

        OrderDto orderDto = initOrder(userDto.getId(), couponForUse, Optional.of(15000L));

        //减掉100账户金额
        Long userBalance = operateUserBalance(userDto.getId(), -10000L);
        assertThat(userBalance, is(0L));

        waitForEventProcessed();

        //订单创建失败
        OrderDto orderDtoFromGet = getOrder(orderDto.getId());
        assertThat(orderDtoFromGet, notNullValue());
        assertThat(orderDtoFromGet.getOrderNo(), is(orderDto.getOrderNo()));
        assertThat(orderDtoFromGet.getStatus(), is(OrderStatus.CREATE_FAILED));

        Long userBalanceAfter = getBalanceByUserId(userDto.getId());
        assertThat(userBalanceAfter, is(userBalance));

        //优惠券依然有效
        if(!couponForUse.isEmpty()) {
            List<CouponDto> usedCoupons = findCouponById(couponForUse);
            assertThat(usedCoupons.size(), is(couponForUse.size()));
            for(CouponDto usedCoupon : usedCoupons) {
                assertThat(usedCoupon.getState(), is(CouponState.VALID));
                assertThat(usedCoupon.getUseTime(), nullValue());
                assertThat(usedCoupon.getOrderId(), nullValue());
            }
        }


    }


    private UserDto initUser() {

        //注册
        String username = RandomStringUtils.randomAlphanumeric(8);
        String password = RandomStringUtils.randomAlphanumeric(8);
        UserDto userDto = registerUser(username, password);

        //等待异步事件处理
        waitForEventProcessed();

        return userDto;
    }

    private Long initBalance(Long userId, Optional<Long> userInitBalance) {
        //账户创建成功
        Long userBalance = getBalanceByUserId(userId);
        assertThat(userBalance, is(0L));

        //添加账户金额
        if(userInitBalance.isPresent()) {
            userBalance = operateUserBalance(userId, userInitBalance.get());
            Long userBalance2 = getBalanceByUserId(userId);
            assertThat(userBalance2, is(userBalance));
        }

        return userBalance;
    }

    private List<Long> initCoupon(Long userId, boolean useCoupon) {

        //优惠券发放成功
        List<CouponDto> userCouponDtoList = findCouponByUser(userId);
        assertThat(userCouponDtoList.size(), is(1));
        CouponDto couponDto = userCouponDtoList.get(0);
        assertThat(couponDto.getState(), is(CouponState.VALID));

        List<Long> couponForUse = new ArrayList<>();
        if(useCoupon) {
            couponForUse = userCouponDtoList.stream().map(CouponDto::getId).collect(Collectors.toList());
        }

        return couponForUse;
    }

    private OrderDto initOrder(Long userId, List<Long> couponForUse, Optional<Long> orderAmount) {

        List<ProductDto> products = findAllProducts();
        assertThat(products.size(), greaterThan(1));

        //优惠券发放成功
        List<CouponDto> userCouponDtoList = findCouponByUser(userId);
        assertThat(userCouponDtoList.size(), is(1));
        CouponDto couponDto = userCouponDtoList.get(0);
        assertThat(couponDto.getState(), is(CouponState.VALID));

        //下订单
        List<PlaceOrderItemDto> orderItemsList = new ArrayList<>();
        if(orderAmount.isPresent()) {
            Long oa = orderAmount.get();
            if(oa % 100 != 0 || oa < 1000L) {
                throw new AssertionError("orderAmount必须为100的倍数并且大于等于1000");
            }
            ProductDto product1 = products.stream()
                    .filter(productDto -> !productDto.getPrice().equals(0L)).findFirst().get();
            ProductDto product2 = products.stream()
                    .filter(productDto -> !productDto.getId().equals(product1.getId())).findFirst().get();
            int quantity2 = 2;
            int quantity1 = (int)((oa - product2.getPrice() * quantity2) / product1.getPrice());
            PlaceOrderItemDto placeOrderItemDto1 = new PlaceOrderItemDto();
            placeOrderItemDto1.setProductId(product1.getId());
            placeOrderItemDto1.setQuantity(quantity1);
            PlaceOrderItemDto placeOrderItemDto2 = new PlaceOrderItemDto();
            placeOrderItemDto2.setProductId(product2.getId());
            placeOrderItemDto2.setQuantity(quantity2);
            orderItemsList.add(placeOrderItemDto1);
            orderItemsList.add(placeOrderItemDto2);

        } else {
            Optional<ProductDto> zeroPriceProduct = products.stream()
                    .filter(productDto -> productDto.getPrice().equals(0L)).findFirst();
            if(!zeroPriceProduct.isPresent()) {
                throw new AssertionError("price为0的product在数据库不存在");
            }
            PlaceOrderItemDto placeOrderItemDto = new PlaceOrderItemDto();
            placeOrderItemDto.setProductId(zeroPriceProduct.get().getId());
            placeOrderItemDto.setQuantity(10);
            orderItemsList.add(placeOrderItemDto);
        }

        PlaceOrderDto placeOrderDto = new PlaceOrderDto();
        placeOrderDto.setUserId(userId);
        placeOrderDto.setPlaceOrderItemList(orderItemsList);
        placeOrderDto.setCouponIdList(couponForUse);

        OrderDto orderDto = placeOrder(placeOrderDto);

        //订单初步创建成功
        assertOrderCreatePending(placeOrderDto, orderDto);

        return orderDto;
    }

    private void placeOrderSuccess(boolean useCoupon, Optional<Long> userInitBalance, Optional<Long> orderAmount) {
        UserDto userDto = initUser();

        Long userBalance = initBalance(userDto.getId(), userInitBalance);

        List<Long> couponForUse = initCoupon(userDto.getId(), useCoupon);

        OrderDto orderDto = initOrder(userDto.getId(), couponForUse, orderAmount);

        waitForEventProcessed();

        //订单创建成功
        assertOrderCreateSuccess(userDto, userBalance, couponForUse, orderDto);
    }

    private Long operateUserBalance(Long userId, Long balance) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(AccountUrl.buildUrl(AccountUrl.ACCOUNT_TRANSACTIONS))
                .queryParam("amount", balance)
                .buildAndExpand(userId).encode().toUri();

        return restTemplate.postForObject(uri, null, Long.class);

    }


    public OrderDto placeOrder(PlaceOrderDto placeOrderDto) {
        String uri = OrderUrl.buildUrl(OrderUrl.PLACE_ORDER);

        return restTemplate.postForObject(uri, createJsonEntity(placeOrderDto), OrderDto.class);
    }

    public OrderDto getOrder(Long orderId) {
        String uri = OrderUrl.buildUrl(OrderUrl.ORDER_INFO);

        return restTemplate.getForObject(uri, OrderDto.class, orderId);
    }

    public Long getBalanceByUserId(Long userId) {

        String uri = AccountUrl.buildUrl(AccountUrl.ACCOUNT_BALANCE);

        ResponseEntity<Long> responseEntity =  restTemplate.getForEntity(uri, Long.class, userId);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        return responseEntity.getBody();
    }


    public UserDto registerUser(String username, String password) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(UserUrl.buildUrl(UserUrl.USER_REGISTER_URL))
                .build().encode().toUri();

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(username);
        registerDto.setPassword(password);

        ResponseEntity<UserDto> responseEntity = restTemplate
                .postForEntity(uri, createJsonEntity(registerDto), UserDto.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        UserDto userDto = responseEntity.getBody();
        assertThat(userDto, notNullValue());
        assertThat(userDto.getUsername(), is(username));
        assertThat(userDto.getId(), notNullValue());

        return userDto;
    }

    public List<CouponDto> findCouponByUser(Long userId) {
        String uri = CouponUrl.buildUrl(CouponUrl.USER_COUPON_LIST_URL);

        ResponseEntity<CouponDto[]> responseEntity = restTemplate.getForEntity(uri, CouponDto[].class, userId);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        CouponDto[] couponDtoList = responseEntity.getBody();
        return Arrays.asList(couponDtoList);
    }

    public List<CouponDto> findCouponById(List<Long> couponIds) {

        URI uri = UriComponentsBuilder
                .fromHttpUrl(CouponUrl.buildUrl(CouponUrl.COUPON_LIST_URL))
                .queryParam("id", couponIds.toArray())
                .build().encode().toUri();

        CouponDto[] couponDtos = restTemplate.getForObject(uri, CouponDto[].class);
        return Arrays.asList(couponDtos);

    }


    public List<ProductDto> findAllProducts() {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(ProductUrl.buildUrl(ProductUrl.ALL_PRODUCT_LIST_URL))
                .build().encode().toUri();

        ProductDto[] productDtos = restTemplate.getForObject(uri, ProductDto[].class);

        return Arrays.asList(productDtos);

    }

    private void assertOrderCreatePending(PlaceOrderDto placeOrderDto, OrderDto orderDto) {
        assertThat(orderDto, notNullValue());
        assertThat(orderDto.getId(), notNullValue());
        assertThat(orderDto.getStatus(), isOneOf(OrderStatus.CREATE_PENDING, OrderStatus.CREATED));
        List<OrderItemDto> orderItemDtoList = orderDto.getOrderItemList();
        List<PlaceOrderItemDto> placeOrderItemList = placeOrderDto.getPlaceOrderItemList();
        assertThat(orderItemDtoList.size(), is(placeOrderItemList.size()));

        for(PlaceOrderItemDto placeOrderItemDto : placeOrderItemList) {
            boolean containsSameProductId = false;
            for(OrderItemDto orderItemDto : orderItemDtoList) {
                if(orderItemDto.getProductId().equals(placeOrderItemDto.getProductId())) {
                    assertThat(orderItemDto.getQuantity(), is(placeOrderItemDto.getQuantity()));
                    containsSameProductId = true;
                    break;
                }
            }
            assertThat(containsSameProductId, is(true));
        }

        assertThat(orderItemDtoList.stream().mapToLong(x -> x.getPrice() * x.getQuantity()).sum(),
                is(orderDto.getTotalAmount()));

        if(!placeOrderDto.getCouponIdList().isEmpty()) {
            assertThat(orderDto.getTotalAmount(), greaterThan(orderDto.getPayAmount()));
        } else {
            assertThat(orderDto.getTotalAmount(), is(orderDto.getPayAmount()));
        }

    }

    private void assertOrderCreateSuccess(UserDto userDto, Long userBalance, List<Long> couponForUse, OrderDto orderDto) {
        OrderDto orderDtoFromGet = getOrder(orderDto.getId());
        assertThat(orderDtoFromGet, notNullValue());
        assertThat(orderDtoFromGet.getOrderNo(), is(orderDto.getOrderNo()));
        assertThat(orderDtoFromGet.getStatus(), is(OrderStatus.CREATED));

        //account余额被正确扣减
        Long userBalanceAfter = getBalanceByUserId(userDto.getId());
        assertThat(userBalanceAfter, is(userBalance - orderDto.getPayAmount()));

        //优惠券已被使用
        if(!couponForUse.isEmpty()) {
            List<CouponDto> usedCoupons = findCouponById(couponForUse);
            assertThat(usedCoupons.size(), is(couponForUse.size()));
            for(CouponDto usedCoupon : usedCoupons) {
                assertThat(usedCoupon.getState(), is(CouponState.USED));
                assertThat(usedCoupon.getUseTime(), notNullValue());
                assertThat(usedCoupon.getOrderId(), is(orderDto.getId()));
            }
        }
    }



}
