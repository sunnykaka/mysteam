package com.akkafun.common.spring;

import com.akkafun.common.event.config.EventConfiguration;
import com.akkafun.common.event.config.SchedulerConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by liubin on 2016/3/28.
 */
@EntityScan(basePackages = {
        "com.akkafun.**.domain",
        "org.springframework.data.jpa.convert.threeten"
})
@EnableJpaRepositories("com.akkafun.**.dao")
@EnableJpaAuditing
@ComponentScan("com.akkafun.**.service")
@Import({EventConfiguration.class, SchedulerConfiguration.class})
public class BaseApplication {


    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return ApplicationContextHolder.getInstance();
    }

    @Bean
    public ApplicationConstant applicationConstant() {
        return new ApplicationConstant();
    }


}