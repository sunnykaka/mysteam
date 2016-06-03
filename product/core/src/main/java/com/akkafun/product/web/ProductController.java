package com.akkafun.product.web;

import com.akkafun.product.api.dtos.ProductDto;
import com.akkafun.product.domain.Product;
import com.akkafun.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.akkafun.product.api.ProductUrl.ALL_PRODUCT_LIST_URL;
import static com.akkafun.product.api.ProductUrl.PRODUCT_LIST_URL;

/**
 * Created by liubin on 2016/3/29.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    @Autowired
    ProductService productService;

    @RequestMapping(value = ALL_PRODUCT_LIST_URL, method = RequestMethod.GET)
    public List<ProductDto> listAllProducts() {

        List<Product> products = productService.findAll();
        return products.stream().map(this::convertProductDto).collect(Collectors.toList());
    }

    @RequestMapping(value = PRODUCT_LIST_URL, method = RequestMethod.GET)
    public List<ProductDto> listProductsById(@RequestParam("id") Long[] ids) {

        List<Long> idList = Arrays.asList(ids);

        List<Product> products = productService.findById(idList);
        Map<Long, ProductDto> couponDtoMap = products.stream()
                .map(this::convertProductDto)
                .collect(Collectors.toMap(ProductDto::getId, Function.identity()));

        return idList.stream().map(couponDtoMap::get).collect(Collectors.toList());
    }


    private ProductDto convertProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setCategory(product.getCategory());
        productDto.setDescription(product.getDescription());
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPrice(product.getPrice());
        return productDto;
    }


}
