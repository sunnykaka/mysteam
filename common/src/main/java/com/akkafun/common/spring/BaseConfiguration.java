package com.akkafun.common.spring;

import com.akkafun.common.utils.JsonUtils;
import com.akkafun.common.utils.spring.CustomRestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by liubin on 2016/3/28.
 */
@EntityScan(basePackages = {
        "com.akkafun.**.domain",
        "org.springframework.data.jpa.convert.threeten"
})
@EnableJpaRepositories("com.akkafun.**.dao")
@EnableJpaAuditing
@EnableHystrix
@ComponentScan({"com.akkafun.**.service", "com.akkafun.**.web"})
public class BaseConfiguration {

    @Bean
    public ApplicationConstant applicationConstant() {
        return new ApplicationConstant();
    }

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return ApplicationContextHolder.getInstance();
    }

    //customize object mapper
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonUtils.OBJECT_MAPPER;
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {

        return CustomRestTemplate.assembleRestTemplate(mappingJackson2HttpMessageConverter);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(objectMapper());
    }


}