package com.akkafun.common.spring;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by liubin on 2016/4/20.
 */
public class ApplicationConstant {

    @Value("${spring.cloud.stream.kafka.binder.zkNodes:}")
    public String zkAddress;

    @Value("${spring.application.name}")
    public String applicationName;

    @Value("${spring.application.index}")
    public int applicationIndex;

}
