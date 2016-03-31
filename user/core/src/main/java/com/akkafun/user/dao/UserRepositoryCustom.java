package com.akkafun.user.dao;


import com.akkafun.common.dao.AbstractRepository;

import java.util.Optional;

/**
 * Created by liubin on 2016/3/29.
 */
public interface UserRepositoryCustom extends AbstractRepository {

    /**
     * 用户名是否存在
     * @param username
     * @param userId
     * @return
     */
    boolean isUsernameExist(String username, Optional<Integer> userId);

}
