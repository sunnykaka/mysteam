package com.akkafun.common.event.service;

import com.akkafun.user.test.BaseTest;
import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.*;
import com.akkafun.common.event.constant.EventProcessStatus;
import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.dao.EventProcessRepository;
import com.akkafun.common.event.dao.EventPublishRepository;
import com.akkafun.common.event.domain.EventProcess;
import com.akkafun.common.event.domain.EventPublish;
import com.google.common.collect.Lists;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.integration.kafka.core.*;
import org.springframework.integration.kafka.util.MessageUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 *
 * Created by liubin on 2016/4/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class EventBusTest extends BaseTest {

    @Autowired
    EventBus eventBus;

    @Autowired
    EventPublishRepository eventPublishRepository;

    @Autowired
    EventProcessRepository eventProcessRepository;

    @Autowired
    BinderFactory<MessageChannel> binderFactory;

    private Decoder<?> keyDecoder = new DefaultDecoder(null);

    private Decoder<?> payloadDecoder = new DefaultDecoder(null);

    @Autowired
    @Qualifier("testEventActivator")
    EventActivator testEventActivator;

    @Autowired
    EventActivator eventActivator;



    /**
     * 测试启动之后监听对象是不是都注册成功了
     */
    @Test
    public void testHandlerRegisterSuccess() {

        EventRegistry eventRegistry = EventRegistry.getInstance();

        Set<EventType> eventTypeSet = eventRegistry.getAllEventType();
        assertThat(eventTypeSet.size(), greaterThan(2));
        assertThat(eventTypeSet, hasItems(EventType.TEST_EVENT_FIRST, EventType.TEST_EVENT_SECOND));

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

        TestEventFirst event = new TestEventFirst("张三", LocalDateTime.now());
        EventPublish eventPublish = eventBus.publish(event);

        EventPublish eventPublishFromDb = eventPublishRepository.findOne(eventPublish.getId());

        assertThat(eventPublishFromDb.getPayload(), is(eventPublish.getPayload()));
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
        assertThat(eventPublishFromDb.getEventType(), is(event.getType()));
        assertThat(eventPublishFromDb.getEventId(), is(event.getId()));

        eventBus.sendUnpublishedEvent();

        //判断状态已经改过来了
        eventPublishFromDb = eventPublishRepository.findOne(eventPublish.getId());
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));

        //判断消息已经发送到kafka
        assertMessageWasSent(event);


    }

    /**
     * 测试recordEvent是否正确的接收了事件并保存到数据库
     * 测试handleUnprocessedEvent是否正确地对未处理事件进行了处理, 并更新了状态
     */
    @Test
    public void testReceiveMessageSuccess() throws InterruptedException {

        TestEventFirst event = new TestEventFirst("张三", LocalDateTime.now());
        EventPublish eventPublish = eventBus.publish(event);
        eventBus.sendUnpublishedEvent();

        //等待事件接收(异步)
        Thread.sleep(3000L);

        EventProcess eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess, notNullValue());
        assertThat(eventProcess.getPayload(), is(eventPublish.getPayload()));
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.NEW));
        assertThat(eventProcess.getEventType(), is(event.getType()));

        List<TestEventFirst> firstEventList = EventHandler.getInstance().getFirstEventList();
        assertThat(firstEventList, empty());
        eventBus.searchAndHandleUnprocessedEvent();

        //等待事件处理(异步)
        Thread.sleep(3000L);

        //判断状态已经改过来了
        eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.PROCESSED));

        //判断事件已经处理
        firstEventList = EventHandler.getInstance().getFirstEventList();
        assertThat(firstEventList, hasItem(event));

    }

    /**
     * 测试EventBus.sendUnpublishedEvent中调用sendMessage抛出异常会不会导致整个事务回滚
     */
    @Test
    public void testExceptionInSendUnpublishedEvent() throws InterruptedException {

        try {
            eventBus.setEventActivator(testEventActivator);

            TestEventFirst firstEvent = new TestEventFirst("张三", LocalDateTime.now());
            EventPublish firstEventPublish = eventBus.publish(firstEvent);
            TestEventSecond secondEvent = new TestEventSecond("张三");
            EventPublish secondEventPublish = eventBus.publish(secondEvent);

            EventPublish firstEventPublishFromDb = eventPublishRepository.findOne(firstEventPublish.getId());
            EventPublish secondEventPublishFromDb = eventPublishRepository.findOne(secondEventPublish.getId());
            assertThat(firstEventPublishFromDb.getPayload(), is(firstEventPublishFromDb.getPayload()));
            assertThat(firstEventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
            assertThat(secondEventPublishFromDb.getPayload(), is(secondEventPublish.getPayload()));
            assertThat(secondEventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));

            eventBus.sendUnpublishedEvent();

            //判断状态已经改过来了
            firstEventPublishFromDb = eventPublishRepository.findOne(firstEventPublish.getId());
            assertThat(firstEventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));
            //这个事件由于抛了异常, 状态没变
            secondEventPublishFromDb = eventPublishRepository.findOne(secondEventPublish.getId());
            assertThat(secondEventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));

        } finally {
            eventBus.setEventActivator(eventActivator);
        }


    }

    /**
     * 测试对于相同id的event, recordEvent方法能够正确的处理数据库报出的唯一键异常.
     */
    @Test
    public void testRecordEventRefuseSameEvent() {

        TestEventFirst event = new TestEventFirst("张三", LocalDateTime.now());
        String eventJson = EventUtils.serializeEvent(event);

        EventProcess eventProcess = eventBus.recordEvent(eventJson);
        assertThat(eventProcess.getId(), notNullValue());
        assertThat(eventProcess.getPayload(), is(eventJson));
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.NEW));
        assertThat(eventProcess.getEventType(), is(event.getType()));

        try {
            eventBus.recordEvent(eventJson);
            throw new AssertionError("预期抛出DataIntegrityViolationException异常, 但是并没有");
        } catch (DataIntegrityViolationException expected) {

        } catch (Throwable e) {
            throw new AssertionError(String.format("预期抛出DataIntegrityViolationException异常, 但是实际是%s, 异常信息%s",
                    e.getClass().getName(), e.getMessage()));

        }

    }




    /**
     * 判断事件已经发送到kafka了
     * @param event
     */
    private void assertMessageWasSent(BaseEvent event) {
        EventType type = event.getType();
        String json = EventUtils.serializeEvent(event);
        String topic = type.toString();
        Binder<MessageChannel, ?, ?> binder = binderFactory.getBinder(null);
        assertThat("binder 不为KafkaMessageChannelBinder, binder: " + binder.getClass().getName(),
                binder instanceof KafkaMessageChannelBinder, is(true));
        KafkaMessageChannelBinder kafkaBinder = (KafkaMessageChannelBinder) binder;
        KafkaTemplate kafkaTemplate = new KafkaTemplate(kafkaBinder.getConnectionFactory());
        Partition partition = new Partition(topic, 0);
        List<FetchRequest> fetchRequestList = Lists.newArrayList(
                new FetchRequest(partition, 0L, 16384)
        );
        Result<KafkaMessageBatch> messageBatchResult = kafkaTemplate.receive(fetchRequestList);
        KafkaMessageBatch kafkaMessageBatch = messageBatchResult.getResult(partition);
        long count = kafkaMessageBatch.getMessages().stream().filter(kafkaMessage ->
                getEventJsonFromKafkaMessage(kafkaMessage).
                        flatMap(x -> Optional.of(x.equals(json))).
                        orElse(false)
        ).count();

        assertThat(count, is(1L));
    }

    /**
     * 从KafkaMessage中取得事件json
     * @param kafkaMessage
     * @return
     */
    private Optional<String> getEventJsonFromKafkaMessage(KafkaMessage kafkaMessage) {
//        Object key = MessageUtils.decodeKey(kafkaMessage, keyDecoder);
        Object value = MessageUtils.decodePayload(kafkaMessage, payloadDecoder);
        try {
            return Optional.of(new String((byte[])value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
