package com.akkafun.user.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by liubin on 2016/3/29.
 */
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public boolean isUsernameExist(String username, Optional<Integer> userId) {

        String hql = "select u.id from User u where u.username = :username";
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        if(userId.isPresent()) {
            hql += " and u.userId != :userId ";
            params.put("userId", userId);
        }
        List<Long> results = query(hql, params);
        return !results.isEmpty();
    }


    @Override
    public EntityManager getEm() {
        return em;
    }
}
