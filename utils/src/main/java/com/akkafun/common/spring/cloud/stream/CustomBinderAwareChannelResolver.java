/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akkafun.common.spring.cloud.stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.binding.BindableChannelFactory;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.binding.DynamicDestinationsBindable;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.BeanFactoryMessageChannelDestinationResolver;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * A {@link org.springframework.messaging.core.DestinationResolver} implementation that
 * resolves the channel from the bean factory and, if not present, creates a new channel
 * and adds it to the factory after binding it to the binder. The binder is optionally
 * determined with a prefix preceding a colon.
 *
 * @author Mark Fisher
 * @author Gary Russell
 * @author Ilayaperumal Gopinathan
 */
public class CustomBinderAwareChannelResolver extends BinderAwareChannelResolver {

    private final BinderFactory<MessageChannel> binderFactory;

    private final ChannelBindingServiceProperties channelBindingServiceProperties;

    private final DynamicDestinationsBindable dynamicDestinationsBindable;

    private final BindableChannelFactory bindableChannelFactory;

    private ConfigurableListableBeanFactory beanFactory;

    public CustomBinderAwareChannelResolver(BinderFactory binderFactory,
                                            ChannelBindingServiceProperties channelBindingServiceProperties,
                                            DynamicDestinationsBindable dynamicDestinationsBindable,
                                            BindableChannelFactory bindableChannelFactory) {
        super(binderFactory, channelBindingServiceProperties, dynamicDestinationsBindable, bindableChannelFactory);
        this.binderFactory = binderFactory;
        this.channelBindingServiceProperties = channelBindingServiceProperties;
        this.dynamicDestinationsBindable = dynamicDestinationsBindable;
        this.bindableChannelFactory = bindableChannelFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public MessageChannel resolveDestination(String channelName) {
        MessageChannel channel = null;
        DestinationResolutionException destinationResolutionException;
        Assert.state(this.beanFactory != null, "No BeanFactory configured");
        try {
            return this.beanFactory.getBean(channelName, MessageChannel.class);
        }
        catch (BeansException ex) {
            destinationResolutionException = new DestinationResolutionException(
                    "Failed to find MessageChannel bean with name '" + channelName + "'", ex);
        }
        synchronized (this) {
            if (this.beanFactory != null && this.binderFactory != null) {
                String[] dynamicDestinations = null;
                if (this.channelBindingServiceProperties != null) {
                    dynamicDestinations = this.channelBindingServiceProperties.getDynamicDestinations();
                }
                boolean dynamicAllowed = ObjectUtils.isEmpty(dynamicDestinations)
                        || ObjectUtils.containsElement(dynamicDestinations, channelName);
                if (dynamicAllowed) {
                    String binderName = null;
                    String beanName = channelName;
                    if (channelName.contains(":")) {
                        String[] tokens = channelName.split(":", 2);
                        if (tokens.length == 2) {
                            binderName = tokens[0];
                            channelName = tokens[1];
                        } else if (tokens.length != 1) {
                            throw new IllegalArgumentException("Unrecognized channel naming scheme: " + channelName + " , should be" +
                                    " [<binder>:]<channelName>");
                        }
                    }
                    channel = this.bindableChannelFactory.createSubscribableChannel(channelName);
                    this.beanFactory.registerSingleton(beanName, channel);
                    channel = (MessageChannel) this.beanFactory.initializeBean(channel, beanName);
                    @SuppressWarnings("unchecked")
                    Binder<MessageChannel, ?, ProducerProperties> binder =
                            (Binder<MessageChannel, ?, ProducerProperties>) binderFactory.getBinder(binderName);
                    ProducerProperties producerProperties = this.channelBindingServiceProperties.getProducerProperties(channelName);
                    String destinationName = this.channelBindingServiceProperties.getBindingDestination(channelName);

                    if (binder instanceof ExtendedPropertiesBinder) {
                        ExtendedPropertiesBinder extendedPropertiesBinder = (ExtendedPropertiesBinder) binder;
                        Object extension = extendedPropertiesBinder.getExtendedProducerProperties(channelName);
                        ExtendedProducerProperties extendedProducerProperties = new ExtendedProducerProperties<>(extension);
                        BeanUtils.copyProperties(producerProperties, extendedProducerProperties);
                        producerProperties = extendedProducerProperties;
                    }

                    this.dynamicDestinationsBindable.addOutputBinding(beanName,
                            binder.bindProducer(destinationName, channel, producerProperties));
                } else {
                    throw destinationResolutionException;
                }
            }
            return channel;
        }
    }
}
