package com.akkafun.common.event.config;

import com.akkafun.common.event.scheduler.EventScheduler;
import com.akkafun.common.event.service.EventBus;
import com.akkafun.common.scheduler.ZkCoordinateScheduledExecutor;
import com.akkafun.common.scheduler.ZkSchedulerCoordinator;
import com.akkafun.common.spring.ApplicationConstant;
import com.akkafun.common.spring.cloud.stream.CustomBinderAwareChannelResolver;
import com.akkafun.common.spring.cloud.stream.CustomChannelBindingService;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binding.BindableChannelFactory;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.binding.ChannelBindingService;
import org.springframework.cloud.stream.binding.DynamicDestinationsBindable;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * Created by liubin on 2016/4/8.
 */
@EnableScheduling
public class SchedulerConfiguration implements SchedulingConfigurer {


    @Bean
    public ZkSchedulerCoordinator zkSchedulerCoordinator(ApplicationConstant applicationConstant){

        return new ZkSchedulerCoordinator(applicationConstant);

    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        TaskScheduler taskScheduler = new ConcurrentTaskScheduler(new ZkCoordinateScheduledExecutor(5));
        taskRegistrar.setTaskScheduler(taskScheduler);

    }

    @Bean
    public EventScheduler eventScheduler(EventBus eventBus) {
        return new EventScheduler(eventBus);
    }

}
