package com.akkafun.order.service.gateway;

import com.akkafun.order.test.OrderBaseTest;
import com.akkafun.product.api.dtos.ProductDto;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/5/9.
 */
public class ProductGatewayTest extends OrderBaseTest {

    @Autowired
    ProductGateway productGateway;

    @Test
    @Ignore
    public void test() {

        List<ProductDto> productDtos = productGateway.findAllProducts();
        assertThat(productDtos.isEmpty(), is(false));

    }


}
