package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.AskParameter;
import com.akkafun.common.event.AskParameterBuilder;
import com.akkafun.common.event.EventTestUtils;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.callbacks.AskTestEventSecondCallback;
import com.akkafun.common.event.callbacks.CallbackParam;
import com.akkafun.common.event.constant.EventProcessStatus;
import com.akkafun.common.event.constant.EventPublishStatus;
import com.akkafun.common.event.dao.*;
import com.akkafun.common.event.domain.*;
import com.akkafun.common.event.handlers.AskTestEventHandler;
import com.akkafun.common.event.handlers.NotifyFirstTestEventFirstHandler;
import com.akkafun.user.test.UserBaseTest;
import com.google.common.collect.Lists;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.integration.kafka.core.*;
import org.springframework.integration.kafka.util.MessageUtils;
import org.springframework.messaging.MessageChannel;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;


/**
 *
 * Created by liubin on 2016/4/11.
 */
public class EventBusTest extends UserBaseTest {

    @Autowired
    EventBus eventBus;

    @Autowired
    EventProcessRepository eventProcessRepository;

    @Autowired
    protected NotifyEventPublishRepository notifyEventPublishRepository;

    @Autowired
    protected AskRequestEventPublishRepository askRequestEventPublishRepository;

    @Autowired
    protected RevokeAskEventPublishRepository revokeAskEventPublishRepository;

    @Autowired
    protected AskResponseEventPublishRepository askResponseEventPublishRepository;

    @Autowired
    BinderFactory<MessageChannel> binderFactory;

    private Decoder<?> keyDecoder = new DefaultDecoder(null);

    private Decoder<?> payloadDecoder = new DefaultDecoder(null);

    @Autowired
    @Qualifier("testEventActivator")
    EventActivator testEventActivator;

    @Autowired
    EventActivator eventActivator;


    @Before
    public void init() throws Exception {
        EventTestUtils.clear();
    }

    /**
     * 测试publish插入Notify记录是否成功
     * 测试sendUnpublishedEvent是否正确的发送了Notify消息并更新了状态
     * 测试recordEvent是否正确的接收了事件并保存到数据库
     * 测试handleUnprocessedEvent是否正确地对未处理事件进行了处理, 并更新了状态
     */
    @Test
    public void testSendAndReceiveNotifyEventSuccess() throws InterruptedException {


        NotifyFirstTestEvent event = new NotifyFirstTestEvent("张三", LocalDateTime.now());
        NotifyEventPublish eventPublish = eventBus.publish(event);

        NotifyEventPublish eventPublishFromDb = notifyEventPublishRepository.findOne(eventPublish.getId());

        assertThat(eventPublishFromDb.getPayload(), is(eventPublish.getPayload()));
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
        assertThat(eventPublishFromDb.getEventType(), is(event.getType()));
        assertThat(eventPublishFromDb.getEventId(), is(event.getId()));

        sendEvent();

        //判断状态已经改过来了
        eventPublishFromDb = notifyEventPublishRepository.findOne(eventPublish.getId());
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));
        //判断消息已经发送到kafka
        assertMessageWasSent(event);


        EventProcess eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess, notNullValue());
        assertThat(eventProcess.getPayload(), is(eventPublish.getPayload()));
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.NEW));
        assertThat(eventProcess.getEventType(), is(event.getType()));

        List<NotifyFirstTestEvent> firstEventList = NotifyFirstTestEventFirstHandler.events;
        List<NotifyFirstTestEvent> secondEventList = NotifyFirstTestEventFirstHandler.events;
        assertThat(firstEventList, empty());
        assertThat(secondEventList, empty());

        handleEvent();

        //判断状态已经改过来了
        eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.PROCESSED));
        //判断事件已经处理
        assertThat(firstEventList, hasItem(event));
        assertThat(secondEventList, hasItem(event));

    }

    /**
     * 发送事件并等待
     */
    private void sendEvent() {

        try {
            eventBus.sendUnpublishedEvent();
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 异步处理事件并等待
     */
    private void handleEvent() {

        try {
            eventBus.searchAndHandleUnprocessedEvent();
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 测试EventBus.sendUnpublishedEvent中调用sendMessage抛出异常会不会导致整个事务回滚
     */
    @Test
    public void testExceptionInSendUnpublishedEvent() throws InterruptedException {

        try {
            eventBus.setEventActivator(testEventActivator);

            NotifyFirstTestEvent firstEvent = new NotifyFirstTestEvent("张三", LocalDateTime.now());
            NotifyEventPublish firstEventPublish = eventBus.publish(firstEvent);
            NotifySecondTestEvent secondEvent = new NotifySecondTestEvent("张三");
            NotifyEventPublish secondEventPublish = eventBus.publish(secondEvent);

            NotifyEventPublish firstEventPublishFromDb = notifyEventPublishRepository.findOne(firstEventPublish.getId());
            NotifyEventPublish secondEventPublishFromDb = notifyEventPublishRepository.findOne(secondEventPublish.getId());
            assertThat(firstEventPublishFromDb.getPayload(), is(firstEventPublishFromDb.getPayload()));
            assertThat(firstEventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
            assertThat(secondEventPublishFromDb.getPayload(), is(secondEventPublish.getPayload()));
            assertThat(secondEventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));

            eventBus.sendUnpublishedEvent();

            //判断状态已经改过来了
            firstEventPublishFromDb = notifyEventPublishRepository.findOne(firstEventPublish.getId());
            assertThat(firstEventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));
            //这个事件由于抛了异常, 状态没变
            secondEventPublishFromDb = notifyEventPublishRepository.findOne(secondEventPublish.getId());
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

        NotifyFirstTestEvent event = new NotifyFirstTestEvent("张三", LocalDateTime.now());
        eventBus.fillEventId(event);
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

    @Test
    public void testSendAndReceiveSuccessAskEvent() {
        testAskEvent(true);
    }

    @Test
    public void testSendAndReceiveFailureAskEvent() {
        testAskEvent(false);
    }




    /**
     * 测试非united ask请求是否发送成功
     * 测试ask请求是否被接收并且handler是否执行成功
     * 测试ask的结果ASK_RESPONSE是否发送
     * 测试ASK_RESPONSE的是否接收并且callback是否执行
     *
     */
    private void testAskEvent(boolean success) {

        String name;
        Map<String, String> paramMap = new HashMap<>();
        if(success) {
            name = AskTestEventHandler.SUCCESS_EVENT_NAME;
            paramMap.put("param1", "v1");
            paramMap.put("param2", "v2");
        } else {
            name = "李四";
            paramMap.put("param3", "v3");
            paramMap.put("param4", "v4");
        }

        AskTestEvent event = new AskTestEvent(name);
        AskParameter askParameter = AskParameterBuilder.ask(event)
                .addParamMap(paramMap)
                .callbackClass(AskTestEventSecondCallback.class).build();

        List<AskRequestEventPublish> askRequestEventPublishList = eventBus.ask(askParameter);

        assertThat(askRequestEventPublishList.size(), is(1));
        AskRequestEventPublish askRequestEventPublish = askRequestEventPublishList.get(0);

        AskRequestEventPublish eventPublishFromDb = askRequestEventPublishRepository.findOne(askRequestEventPublish.getId());

        assertThat(eventPublishFromDb.getPayload(), is(askRequestEventPublish.getPayload()));
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.NEW));
        assertThat(eventPublishFromDb.getEventType(), is(event.getType()));
        assertThat(eventPublishFromDb.getEventId(), is(event.getId()));

        sendEvent();

        //判断状态已经改过来了
        eventPublishFromDb = askRequestEventPublishRepository.findOne(askRequestEventPublish.getId());
        assertThat(eventPublishFromDb.getStatus(), is(EventPublishStatus.PUBLISHED));
        //判断消息已经发送到kafka
        assertMessageWasSent(event);

        EventProcess eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess, notNullValue());
        assertThat(eventProcess.getPayload(), is(eventPublishFromDb.getPayload()));
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.NEW));
        assertThat(eventProcess.getEventType(), is(event.getType()));

        List<AskTestEvent> askTestEvents = AskTestEventHandler.events;
        assertThat(askTestEvents, empty());

        //处理askEvent事件
        handleEvent();

        //判断状态已经改过来了
        eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess.getStatus(), is(EventProcessStatus.PROCESSED));
        //判断事件已经处理
        assertThat(askTestEvents, hasItem(event));

        //发送askResponse响应
        sendEvent();
        //处理ASK_RESPONSE事件
        handleEvent();

        //判断对应的回调函数已经被调用
        List<CallbackParam> callbackParams = success ? AskTestEventSecondCallback.successParams
                : AskTestEventSecondCallback.failureParams;
        assertThat(callbackParams.size(), is(1));
        CallbackParam callbackParam = callbackParams.get(0);
        assertThat(callbackParam.getAskEvents().size(), is(1));
        assertThat(callbackParam.getParams(), is(paramMap));
        if(!success) {
            FailureInfo failureInfo = callbackParam.getFailureInfo();
            assertThat(failureInfo, notNullValue());
            assertThat(failureInfo.getReason(), is(FailureReason.FAILED));
            assertThat(failureInfo.getFailureTime(), notNullValue());
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
        List<FetchRequest> fetchRequestList = Lists.newArrayList(new FetchRequest(partition, 0L, 16384));
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
