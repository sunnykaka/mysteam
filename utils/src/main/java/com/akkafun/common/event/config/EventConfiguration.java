package com.akkafun.common.event.config;

import com.akkafun.common.spring.cloud.stream.CustomChannelBindingService;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binding.ChannelBindingService;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;

/**
 * Created by liubin on 2016/4/8.
 */
public class EventConfiguration {

    @Bean
    public ChannelBindingService bindingService(ChannelBindingServiceProperties channelBindingServiceProperties,
                                                BinderFactory<MessageChannel> binderFactory) {

        return new CustomChannelBindingService(channelBindingServiceProperties, binderFactory);

    }


}
