package com.akkafun.common.spring.cloud.stream;

import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.binding.ChannelBindingService;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.messaging.MessageChannel;

import java.util.*;

/**
 * Created by liubin on 2016/4/8.
 */
public class CustomChannelBindingService extends ChannelBindingService {

    private BinderFactory<MessageChannel> binderFactory;

    private final ChannelBindingServiceProperties channelBindingServiceProperties;

    private final Map<String, List<Binding<MessageChannel>>> producerBindings = new HashMap<>();

    private final Map<String, List<Binding<MessageChannel>>> consumerBindings = new HashMap<>();


    public CustomChannelBindingService(ChannelBindingServiceProperties channelBindingServiceProperties,
                                       BinderFactory<MessageChannel> binderFactory) {
        super(channelBindingServiceProperties, binderFactory);
        this.channelBindingServiceProperties = channelBindingServiceProperties;
        this.binderFactory = binderFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Binding<MessageChannel>> bindConsumer(MessageChannel inputChannel, String inputChannelName) {
        String[] channelBindingTargets = new String[]{"testq1"};
        List<Binding<MessageChannel>> bindings = new ArrayList<>();
        Binder<MessageChannel, ConsumerProperties, ?> binder =
                (Binder<MessageChannel, ConsumerProperties, ?>) getBinderForChannel(inputChannelName);
        ConsumerProperties consumerProperties =
                this.channelBindingServiceProperties.getConsumerProperties(inputChannelName);
        if (binder instanceof ExtendedPropertiesBinder) {
            ExtendedPropertiesBinder extendedPropertiesBinder = (ExtendedPropertiesBinder) binder;
            Object extension = extendedPropertiesBinder.getExtendedConsumerProperties(inputChannelName);
            ExtendedConsumerProperties extendedConsumerProperties = new ExtendedConsumerProperties(extension);
            BeanUtils.copyProperties(consumerProperties, extendedConsumerProperties);
            consumerProperties = extendedConsumerProperties;
        }
        for (String target : channelBindingTargets) {
            Binding<MessageChannel> binding = binder.bindConsumer(target, channelBindingServiceProperties.getGroup(inputChannelName), inputChannel, consumerProperties);
            bindings.add(binding);
        }
        this.consumerBindings.put(inputChannelName, bindings);
        return bindings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Binding<MessageChannel> bindProducer(MessageChannel outputChannel, String outputChannelName) {
        String[] channelBindingTargets = new String[]{"testq2"};
        List<Binding<MessageChannel>> bindings = new ArrayList<>();
        Binder<MessageChannel, ?, ProducerProperties> binder =
                (Binder<MessageChannel, ?, ProducerProperties>) getBinderForChannel(outputChannelName);
        ProducerProperties producerProperties = this.channelBindingServiceProperties.getProducerProperties(outputChannelName);
        if (binder instanceof ExtendedPropertiesBinder) {
            ExtendedPropertiesBinder extendedPropertiesBinder = (ExtendedPropertiesBinder) binder;
            Object extension = extendedPropertiesBinder.getExtendedProducerProperties(outputChannelName);
            ExtendedProducerProperties extendedProducerProperties = new ExtendedProducerProperties<>(extension);
            BeanUtils.copyProperties(producerProperties, extendedProducerProperties);
            producerProperties = extendedProducerProperties;
        }
        Binding<MessageChannel> binding = null;
        for (String target : channelBindingTargets) {
            binding = binder.bindProducer(target, outputChannel, producerProperties);
            bindings.add(binding);
        }
        this.producerBindings.put(outputChannelName, bindings);
        //return the last binding.
        return binding;
    }

    private Binder<MessageChannel, ?, ?> getBinderForChannel(String channelName) {
        String transport = this.channelBindingServiceProperties.getBinder(channelName);
        return binderFactory.getBinder(transport);
    }

}
