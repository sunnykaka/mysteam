package com.akkafun.order.dao;

import com.akkafun.order.domain.Product;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/4/26.
 */
public interface ProductRepository extends PagingAndSortingRepository<Product, Long>{
}
