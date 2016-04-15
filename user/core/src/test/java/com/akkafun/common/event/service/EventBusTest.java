package com.akkafun.common.event.service;

import com.akkafun.BaseTest;
import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.event.EventRegistry;
import com.akkafun.common.event.EventSubscriber;
import com.akkafun.common.event.EventTestApplication;
import com.akkafun.common.event.TestEventFirst;
import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.dao.EventProcessRepository;
import com.akkafun.common.event.dao.EventPublishRepository;
import com.akkafun.common.event.domain.EventProcess;
import com.akkafun.common.event.domain.EventPublish;
import com.akkafun.user.Application;
import com.google.common.collect.Lists;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.config.ChannelBindingServiceProperties;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.kafka.core.*;
import org.springframework.integration.kafka.util.MessageUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * 1. 测试启动之后监听对象是不是都注册成功了
 * 2. 测试publish插入记录是否成功
 * 3. 测试sendUnpublishedEvent是否正确的发送了消息并更新了状态
 * 4. 测试recordEvent是否正确的接收了事件并保存到数据库
 * 5. 测试handleUnprocessedEvent是否正确地对未处理事件进行了处理, 并更新了状态
 *
 * Created by liubin on 2016/4/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EventTestApplication.class)
public class EventBusTest extends BaseTest {

    @Autowired
    EventBus eventBus;

    @Autowired
    EventPublishRepository eventPublishRepository;

    @Autowired
    EventProcessRepository eventProcessRepository;

    @Autowired
    BinderFactory<MessageChannel> binderFactory;

    @Autowired
    ChannelBindingServiceProperties channelBindingServiceProperties;

    private Decoder<?> keyDecoder = new DefaultDecoder(null);

    private Decoder<?> payloadDecoder = new DefaultDecoder(null);




    /**
     * 测试启动之后监听对象是不是都注册成功了
     */
    @Test
    public void testHandlerRegisterSuccess() {

        EventRegistry eventRegistry = EventRegistry.getInstance();

        Set<EventType> eventTypeSet = eventRegistry.getAllEventType();
        assertThat(eventTypeSet.size(), is(2));
        assertThat(eventTypeSet, contains(EventType.TEST_EVENT_FIRST, EventType.TEST_EVENT_SECOND));

        Set<EventSubscriber> eventFirstSubscriberSet = eventRegistry.findEventSubscriberByType(EventType.TEST_EVENT_FIRST);
        Set<EventSubscriber> eventSecondSubscriberSet = eventRegistry.findEventSubscriberByType(EventType.TEST_EVENT_SECOND);
        assertThat(eventFirstSubscriberSet.size(), is(1));
        assertThat(eventSecondSubscriberSet.size(), is(1));

    }

    /**
     * 测试publish插入记录是否成功
     * 测试sendUnpublishedEvent是否正确的发送了消息并更新了状态
     */
    @Test
    public void testPublishAndSendEventSuccess() {


        String inputChannelName = Processor.INPUT;
        String topic = TestEventFirst.EVENT_TYPE.toString();
        Binder<MessageChannel, ?, ?> binder = binderFactory.getBinder(null);
        assertThat("binder 不为KafkaMessageChannelBinder, binder: " + binder.getClass().getName(),
                binder instanceof KafkaMessageChannelBinder, is(true));
        KafkaMessageChannelBinder kafkaBinder = (KafkaMessageChannelBinder) binder;
        KafkaTemplate kafkaTemplate = new KafkaTemplate(kafkaBinder.getConnectionFactory());
        Partition partition = new Partition(topic, 0);
        List<FetchRequest> fetchRequestList = Lists.newArrayList(
                new FetchRequest(partition, 0L, 1024 * 1024 * 1024)
        );
        Result<KafkaMessageBatch> messageBatchResult = kafkaTemplate.receive(fetchRequestList);
        System.out.println(messageBatchResult.getResults().size());
        KafkaMessageBatch kafkaMessageBatch = messageBatchResult.getResult(partition);
        KafkaMessage kafkaMessage = kafkaMessageBatch.getMessages().get(0);
        Object key = MessageUtils.decodeKey(kafkaMessage, keyDecoder);
        Object value = MessageUtils.decodePayload(kafkaMessage, payloadDecoder);
        System.out.println(key);


//        TestEventFirst event = new TestEventFirst("张三", LocalDateTime.now());
//        EventPublish eventPublish = eventBus.publish(event);
//
//        EventPublish eventPublishFromDb = eventPublishRepository.findOne(eventPublish.getId());
//
//        assertThat(eventPublishFromDb.getPayload(), is(eventPublish.getPayload()));
//        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
//
//        eventBus.sendUnpublishedEvent();
//
//        //判断状态已经改过来了
//        eventPublishFromDb = eventPublishRepository.findOne(eventPublish.getId());
//        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));
//
//        //判断消息已经发送到kafka
//        EventType type = event.getType();



    }


}
