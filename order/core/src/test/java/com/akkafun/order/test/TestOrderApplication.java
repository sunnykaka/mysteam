package com.akkafun.order.test;

import com.akkafun.common.event.config.EventConfiguration;
import com.akkafun.common.spring.BaseConfiguration;
import com.akkafun.common.spring.ServiceClientConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import({BaseConfiguration.class, EventConfiguration.class, ServiceClientConfiguration.class})
public class TestOrderApplication {


}