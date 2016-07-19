package com.akkafun.product.service;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.base.exception.AppBusinessException;
import com.akkafun.product.api.dtos.ProductDto;
import com.akkafun.product.dao.ProductRepository;
import com.akkafun.product.domain.Product;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liubin on 2016/5/16.
 */
@Service
public class ProductService {

    protected Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> findAll() {

        return Lists.newArrayList(productRepository.findAll());
    }

    /**
     * 查询产品列表, 如果对应id在数据库不存在, 返回的List的位置对应null值.
     * 入参数组长度一定等于返回的List长度.
     *
     * @param idList
     * @return
     */
    @Transactional(readOnly = true)
    public List<Product> findById(List<Long> idList) {

        if(idList == null || idList.isEmpty()) return new ArrayList<>();
        if(idList.size() > 50) throw new AppBusinessException(CommonErrorCode.BAD_REQUEST, "一次查询的id数量不能超过50");

        List<Product> productList = Lists.newArrayList(productRepository.findAll(idList));

        if (!productList.isEmpty()) {
            List<Long> productIdList = productList.stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            //过滤出根据接口查询不到的产品id列表
            List<Long> notExistIdList = idList.stream()
                    .filter(productId -> !productIdList.contains(productId))
                    .collect(Collectors.toList());

            if (!notExistIdList.isEmpty()) {
                throw new AppBusinessException(CommonErrorCode.NOT_FOUND,
                        String.format("不存在的产品id: %s", notExistIdList.toString()));
            }
        }

        return productList;

    }
}
