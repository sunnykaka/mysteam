package com.akkafun.user.dao;

import com.akkafun.user.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by liubin on 2016/3/29.
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long>, UserRepositoryCustom {



}
