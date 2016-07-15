package com.akkafun.user.web;

import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.common.utils.TestUtils;
import com.akkafun.user.api.UserErrorCode;
import com.akkafun.user.test.UserBaseControllerTest;
import com.akkafun.user.api.UserUrl;
import com.akkafun.user.api.dtos.RegisterDto;
import com.akkafun.user.api.dtos.UserDto;
import com.akkafun.user.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import static com.akkafun.common.utils.TestUtils.createJsonEntity;

/**
 * Created by liubin on 2016/3/29.
 */
public class UserControllerTest extends UserBaseControllerTest {

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testRegister() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(RandomStringUtils.randomAlphanumeric(8));
        registerDto.setPassword(RandomStringUtils.randomAlphanumeric(8));

        UserDto userDto = restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                createJsonEntity(registerDto), UserDto.class);

        assertThat(userDto, notNullValue());
        assertThat(userDto.getId(), notNullValue());
        assertThat(userDto.getUsername(), notNullValue());

    }

    /**
     * 测试用户名已存在的异常处理(AppBusinessException)
     */
    @Test
    public void testRegisterWithExistUsername() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(RandomStringUtils.randomAlphanumeric(8));
        registerDto.setPassword(RandomStringUtils.randomAlphanumeric(8));

        UserDto userDto = restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                createJsonEntity(registerDto), UserDto.class);

        assertThat(userDto, notNullValue());
        assertThat(userDto.getId(), notNullValue());
        assertThat(userDto.getUsername(), notNullValue());

        TestUtils.assertServerError(
                () -> restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                        createJsonEntity(registerDto), UserDto.class),
                error -> {
                    assertThat(error.getCode(), is(UserErrorCode.UsernameExist.getCode()));
                    assertThat(error.getRequestUri(), notNullValue());
                }
        );

    }


    /**
     * 测试密码为空, 密码长度过长的异常处理(MethodArgumentNotValidException)
     */
    @Test
    public void testRegisterWithInvalidParam() {

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(RandomStringUtils.randomAlphanumeric(8));

        TestUtils.assertServerError(
                () -> restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                        createJsonEntity(registerDto), Object.class),
                error -> {
                    assertThat(error.getCode(), is(CommonErrorCode.BAD_REQUEST.getCode()));
                    assertThat(error.getRequestUri(), notNullValue());
                }
        );

        registerDto.setPassword(RandomStringUtils.randomAlphanumeric(30));
        TestUtils.assertServerError(
                () -> restTemplate.postForObject(buildRequestUrl(UserUrl.USER_REGISTER_URL),
                        createJsonEntity(registerDto), Object.class),
                error -> {
                    assertThat(error.getCode(), is(CommonErrorCode.BAD_REQUEST.getCode()));
                    assertThat(error.getRequestUri(), notNullValue());
                }
        );


    }

//    /**
//     * 测试访问不存在页面的异常处理(404)
//     */
//    @Test
//    public void testServer404Error() {
//
//        TestUtils.assertServerError(
//                () -> restTemplate.getForObject(buildRequestUrl("/A_URL_NOT_EXIST"), Object.class),
//                error -> {
//                    System.out.println(error);
//                    assertThat(error.getCode(), is(CommonErrorCode.NOT_FOUND.getCode()));
//                    assertThat(error.getMessage(), notNullValue());
//                }
//        );
//
//    }


}
