package com.akkafun.common;

import com.akkafun.common.event.config.EventConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by liubin on 2016/3/28.
 */
@SpringBootApplication
@EntityScan(basePackages = {
        "org.springframework.data.jpa.convert.threeten"
})
@EnableJpaRepositories("com.akkafun.*.dao")
@EnableJpaAuditing
@ComponentScan("com.akkafun.*.service")
@Import(EventConfiguration.class)
public class TestApplication {

}