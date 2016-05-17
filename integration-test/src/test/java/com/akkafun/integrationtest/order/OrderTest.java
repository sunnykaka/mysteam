package com.akkafun.integrationtest.order;

import com.akkafun.coupon.api.CouponUrl;
import com.akkafun.coupon.api.constants.CouponState;
import com.akkafun.coupon.api.dtos.CouponDto;
import com.akkafun.integrationtest.test.BaseIntegrationTest;
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
import java.util.Arrays;
import java.util.List;

import static com.akkafun.common.test.TestUtils.createJsonEntity;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/3/29.
 */
public class OrderTest extends BaseIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void test() throws InterruptedException {

        String username = RandomStringUtils.randomAlphanumeric(8);
        String password = RandomStringUtils.randomAlphanumeric(8);
        UserDto userDto = registerUser(username, password);

        Thread.sleep(3000L);
        List<CouponDto> userCouponDtoList = findCouponByUser(userDto.getId());
        assertThat(userCouponDtoList.size(), is(1));
        CouponDto couponDto = userCouponDtoList.get(0);
        assertThat(couponDto.getState(), is(CouponState.VALID));

        List<ProductDto> products = findAllProducts();
        assertThat(products.isEmpty(), is(false));

        //TODO place order

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



}
