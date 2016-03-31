package com.akkafun.user.web;

import com.akkafun.common.utils.JsonUtils;
import com.akkafun.user.BaseControllerTest;
import com.akkafun.user.api.UserUrl;
import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.api.dtos.UserDto;
import com.akkafun.user.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/3/29.
 */
public class UserControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testRegister() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(RandomStringUtils.randomAlphanumeric(8));
        registerDto.setPassword(RandomStringUtils.randomAlphanumeric(8));

        UserDto userDto = restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                createJsonEntity(JsonUtils.object2Json(registerDto)), UserDto.class);

        assertThat(userDto, notNullValue());
        assertThat(userDto.getId(), notNullValue());
        assertThat(userDto.getUsername(), notNullValue());

    }




}
