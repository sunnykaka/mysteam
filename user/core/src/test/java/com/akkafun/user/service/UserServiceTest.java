package com.akkafun.user.service;

import com.akkafun.BaseTest;
import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/3/29.
 */
public class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Test
    @Transactional
    @Rollback(true)
    public void test() {
        User user = new User();
        user.setUsername("张三");
        user.setPassword("123456");

        userService.save(user);

        assertThat(user.getId(), notNullValue());
        System.out.println("user id: " + user.getId());
    }

    @Test
    public void test2() {
        Optional<User> user = userService.getById(1L);

        assertThat(user.isPresent(), is(true));
        System.out.println(user.get().getId());
    }

    @Test
    public void testRegister() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(RandomStringUtils.randomAlphanumeric(8));
        registerDto.setPassword(RandomStringUtils.randomAlphanumeric(8));

        User user = userService.register(registerDto);
        assertThat(user.getId(), notNullValue());

        Optional<User> userOp = userService.getById(user.getId());
        assertThat(userOp.isPresent(), is(true));

        try {
            userService.register(registerDto);
            throw new AssertionError("使用重复用户名注册成功: " + registerDto.getUsername());
        } catch(RuntimeException e) {
            System.out.println(e.getMessage());
        }

    }




}
