package com.akkafun.user;

import com.akkafun.common.event.config.EventConfiguration;
import com.akkafun.common.spring.cloud.stream.CustomChannelBindingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binding.ChannelBindingService;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.messaging.MessageChannel;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@EntityScan(basePackages = {
        "com.akkafun.*.domain",
        "org.springframework.data.jpa.convert.threeten"
})
//@EnableJpaRepositories("com.akkafun.*.dao")
@EnableJpaAuditing
//@ComponentScan("com.akkafun.*.service")
@Import(EventConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}