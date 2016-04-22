package com.akkafun.common.spring.cloud.stream;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.event.EventRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.binding.ChannelBindingService;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义类与父类的区别:
 * 重新定义bindConsumer, 因为在启动的时候需要监听事件的都调用了EventRegistry.register.
 * 所以EventRegistry已经包含了所有的感兴趣的事件类型(即topic), 在这里就将所有感兴趣的topic注册到Processor.INPUT这个channel
 *
 * Created by liubin on 2016/4/8.
 *
 */
public class CustomChannelBindingService extends ChannelBindingService {

    private final Log log = LogFactory.getLog(CustomChannelBindingService.class);

    private BinderFactory<MessageChannel> binderFactory;

    private final ChannelBindingServiceProperties channelBindingServiceProperties;

    private final Map<String, List<Binding<MessageChannel>>> consumerBindings = new HashMap<>();

    private final EventRegistry eventRegistry = EventRegistry.getInstance();

    public CustomChannelBindingService(ChannelBindingServiceProperties channelBindingServiceProperties,
                                       BinderFactory<MessageChannel> binderFactory) {
        super(channelBindingServiceProperties, binderFactory);
        this.channelBindingServiceProperties = channelBindingServiceProperties;
        this.binderFactory = binderFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Binding<MessageChannel>> bindConsumer(MessageChannel inputChannel, String inputChannelName) {
        Set<EventType> eventTypeSet = eventRegistry.getAllEventType();
        String[] channelBindingTargets = eventTypeSet.stream().
                map(EventType::name).collect(Collectors.toList()).toArray(new String[eventTypeSet.size()]);
        if(log.isInfoEnabled()) {
            log.info("spring kafka consumer bind to these topics: " + Arrays.toString(channelBindingTargets));
        }
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

    @Override
    public void unbindConsumers(String inputChannelName) {
        List<Binding<MessageChannel>> bindings = this.consumerBindings.remove(inputChannelName);
        if (bindings != null && !CollectionUtils.isEmpty(bindings)) {
            for (Binding<MessageChannel> binding : bindings) {
                binding.unbind();
            }
        }
        else if (log.isWarnEnabled()) {
            log.warn("Trying to unbind channel '" + inputChannelName + "', but no binding found.");
        }
    }

    private Binder<MessageChannel, ?, ?> getBinderForChannel(String channelName) {
        String transport = this.channelBindingServiceProperties.getBinder(channelName);
        return binderFactory.getBinder(transport);
    }

}
