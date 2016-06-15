package com.akkafun.integrationtest.test;

import com.akkafun.common.spring.BaseApplication;
import com.akkafun.common.spring.ServiceClientApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import({BaseApplication.class, ServiceClientApplication.class})
public class IntegrationTestApplication {

}