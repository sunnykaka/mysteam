package com.akkafun.integrationtest.order;

import com.akkafun.account.context.AccountApplication;
import com.akkafun.base.api.CommonErrorCode;
import com.akkafun.common.test.TestUtils;
import com.akkafun.common.utils.JsonUtils;
import com.akkafun.integrationtest.test.BaseIntegrationTest;
import com.akkafun.user.api.UserErrorCode;
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

/**
 * Created by liubin on 2016/3/29.
 */
public class OrderTest extends BaseIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void test() {

        AccountApplication.main(new String[0]);

    }


}
