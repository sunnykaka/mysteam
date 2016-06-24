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
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.akkafun.common.utils.TestUtils.createJsonEntity;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/3/29.
 */
public class OrderTest extends BaseIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testCreateOrderSuccess() throws InterruptedException {

        //注册
        String username = RandomStringUtils.randomAlphanumeric(8);
        String password = RandomStringUtils.randomAlphanumeric(8);
        UserDto userDto = registerUser(username, password);

        //等待异步事件处理
        waitForEventProcessed();

        //校验优惠券发放成功
        List<CouponDto> userCouponDtoList = findCouponByUser(userDto.getId());
        assertThat(userCouponDtoList.size(), is(1));
        CouponDto couponDto = userCouponDtoList.get(0);
        assertThat(couponDto.getState(), is(CouponState.VALID));

        List<ProductDto> products = findAllProducts();
        assertThat(products.size(), greaterThan(1));

        //TODO 校验账户创建成功


        //下订单
        int index = new Random().nextInt(products.size());
        ProductDto product1 = products.get(index);
        ProductDto product2 = products.get((index + 1) % products.size());
        int quantity1 = 1;
        int quantity2 = 2;

        PlaceOrderDto placeOrderDto = new PlaceOrderDto();

        placeOrderDto.setUserId(userDto.getId());
        PlaceOrderItemDto placeOrderItemDto1 = new PlaceOrderItemDto();
        placeOrderItemDto1.setProductId(product1.getId());
        placeOrderItemDto1.setQuantity(quantity1);
        PlaceOrderItemDto placeOrderItemDto2 = new PlaceOrderItemDto();
        placeOrderItemDto2.setProductId(product2.getId());
        placeOrderItemDto2.setQuantity(quantity2);
        placeOrderDto.setPlaceOrderItemList(Lists.newArrayList(placeOrderItemDto1, placeOrderItemDto2));

        //下订单不带优惠券
        OrderDto orderDto = placeOrder(placeOrderDto);
        //校验订单初步创建成功
        assertOrderCreatePending(placeOrderDto, orderDto);

        //等待ask事件被account service和coupon service接收并处理, 返回结果被order service接收并处理
        //假设定时器轮询间隔为300ms, queue延迟为0
        //理论最大处理时间 = ask发送事件 + queue延迟 + as接收事件 + as处理事件 + as发送事件 + queue延迟
        // + os接收事件 + os处理事件 = 300 * 6 = 1800ms
        waitForEventProcessed();

        OrderDto orderDtoFromGet = getOrder(orderDto.getId());
        assertThat(orderDtoFromGet, notNullValue());
        assertThat(orderDtoFromGet.getOrderNo(), is(orderDto.getOrderNo()));
        assertThat(orderDtoFromGet.getStatus(), is(OrderStatus.CREATED));

        //TODO 校验account余额被正确扣减

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

        URI uri = UriComponentsBuilder
                .fromHttpUrl(AccountUrl.buildUrl(AccountUrl.ACCOUNT_BALANCE))
                .queryParam("userId", userId)
                .build().encode().toUri();

        return restTemplate.getForObject(uri, Long.class);
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
        assertThat(orderDto.getStatus(), is(OrderStatus.CREATE_PENDING));
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



}
