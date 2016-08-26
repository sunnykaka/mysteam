package com.akkafun.order.service.gateway;

import com.akkafun.product.api.ProductUrl;
import com.akkafun.product.api.dtos.ProductDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by liubin on 2016/8/25.
 */
@FeignClient(ProductUrl.SERVICE_HOSTNAME)
public interface ProductClient {

    @RequestMapping(method = RequestMethod.GET, value = ProductUrl.PRODUCT_LIST_URL)
    List<ProductDto> findProducts(@RequestParam("id") List<Long> id);

    @RequestMapping(method = RequestMethod.GET, value = ProductUrl.ALL_PRODUCT_LIST_URL)
    List<ProductDto> findAllProducts();

}
