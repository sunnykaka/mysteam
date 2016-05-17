package com.akkafun.product.dao;

import com.akkafun.product.domain.Product;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/4/26.
 */
public interface ProductRepository extends PagingAndSortingRepository<Product, Long>{
}
