package com.akkafun.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}