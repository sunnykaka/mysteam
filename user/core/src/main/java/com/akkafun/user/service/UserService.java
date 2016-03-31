package com.akkafun.user.service;

import com.akkafun.common.utils.PasswordHash;
import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.dao.UserRepository;
import com.akkafun.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.Optional;

/**
 * Created by liubin on 2016/3/29.
 */
@Service
public class UserService {

    protected Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(Long userId) {
        return Optional.ofNullable(userRepository.findOne(userId));
    }

    @Transactional
    public User register(RegisterDto registerDto) {
        if(isUsernameExist(registerDto.getUsername(), Optional.empty())) {
            throw new RuntimeException(String.format("用户名%s已存在", registerDto.getUsername()));
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        try {
            user.setPassword(PasswordHash.createHash(registerDto.getPassword()));
        } catch (GeneralSecurityException e) {
            logger.error("创建哈希密码的时候发生错误", e);
            throw new RuntimeException("用户注册失败");
        }

        userRepository.save(user);

        return user;
    }


    /**
     * 判断用户名是否存在
     * @param username
     * @param userId 当前用户ID,如果是修改用户的话,需要传,否则可以传empty
     * @return
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExist(String username, Optional<Integer> userId) {

        return userRepository.isUsernameExist(username, userId);
    }


}
