package com.akkafun.product.context;

import com.akkafun.common.scheduler.config.SchedulerConfiguration;
import com.akkafun.common.spring.BaseApplication;
import com.akkafun.common.spring.ServiceClientApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@Import({BaseApplication.class, SchedulerConfiguration.class, ServiceClientApplication.class})
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }


}