package com.akkafun.common.event.service;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.constants.FailureReason;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.event.AskParameter;
import com.akkafun.common.event.AskParameterBuilder;
import com.akkafun.common.event.EventTestUtils;
import com.akkafun.common.event.EventUtils;
import com.akkafun.common.test.callbacks.AskTestEventSecondCallback;
import com.akkafun.common.test.callbacks.CallbackParam;
import com.akkafun.common.test.callbacks.UnitedTestEventCallback;
import com.akkafun.common.event.constant.ProcessStatus;
import com.akkafun.common.event.dao.*;
import com.akkafun.common.event.domain.*;
import com.akkafun.common.test.domain.AskTestEvent;
import com.akkafun.common.test.domain.NotifyFirstTestEvent;
import com.akkafun.common.test.domain.NotifySecondTestEvent;
import com.akkafun.common.test.domain.RevokableAskTestEvent;
import com.akkafun.common.test.handlers.AskTestEventHandler;
import com.akkafun.common.test.handlers.NotifyFirstTestEventFirstHandler;
import com.akkafun.common.test.handlers.RevokableAskTestEventHandler;
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

    @Autowired
    protected EventWatchService eventWatchService;



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
        assertThat(eventPublishFromDb.getStatus(), is(ProcessStatus.NEW));
        assertThat(eventPublishFromDb.getEventType(), is(event.getType()));
        assertThat(eventPublishFromDb.getEventId(), is(event.getId()));

        sendEvent();

        //判断状态已经改过来了
        eventPublishFromDb = notifyEventPublishRepository.findOne(eventPublish.getId());
        assertThat(eventPublishFromDb.getStatus(), is(ProcessStatus.PROCESSED));
        //判断消息已经发送到kafka
//        assertMessageWasSent(event);


        EventProcess eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess, notNullValue());
        assertThat(eventProcess.getPayload(), is(eventPublish.getPayload()));
        assertThat(eventProcess.getStatus(), is(ProcessStatus.NEW));
        assertThat(eventProcess.getEventType(), is(event.getType()));

        List<NotifyFirstTestEvent> firstEventList = NotifyFirstTestEventFirstHandler.events;
        List<NotifyFirstTestEvent> secondEventList = NotifyFirstTestEventFirstHandler.events;
        assertThat(firstEventList, empty());
        assertThat(secondEventList, empty());

        handleEvent();

        //判断状态已经改过来了
        eventProcess = eventProcessRepository.getByEventId(event.getId());
        assertThat(eventProcess.getStatus(), is(ProcessStatus.PROCESSED));
        //判断事件已经处理
        assertThat(firstEventList, hasItem(event));
        assertThat(secondEventList, hasItem(event));

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
            assertThat(firstEventPublishFromDb.getStatus(), is(ProcessStatus.NEW));
            assertThat(secondEventPublishFromDb.getPayload(), is(secondEventPublish.getPayload()));
            assertThat(secondEventPublishFromDb.getStatus(), is(ProcessStatus.NEW));

            eventBus.sendUnpublishedEvent();

            //判断状态已经改过来了
            firstEventPublishFromDb = notifyEventPublishRepository.findOne(firstEventPublish.getId());
            assertThat(firstEventPublishFromDb.getStatus(), is(ProcessStatus.PROCESSED));
            //这个事件由于抛了异常, 状态没变
            secondEventPublishFromDb = notifyEventPublishRepository.findOne(secondEventPublish.getId());
            assertThat(secondEventPublishFromDb.getStatus(), is(ProcessStatus.NEW));

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
        assertThat(eventProcess.getStatus(), is(ProcessStatus.NEW));
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
        testAskEvent(true, false);
    }

    @Test
    public void testSendAndReceiveFailureAskEvent() {
        testAskEvent(false, false);
    }

    @Test
    public void testSendAndReceiveSuccessUnitedAskEvent() {
        testAskEvent(true, true);
    }

    @Test
    public void testSendAndReceiveFailureUnitedAskEvent() {
        testAskEvent(false, true);
    }



    /**
     * 测试ask请求是否发送成功
     * 测试ask请求是否被接收并且handler是否执行成功
     * 测试ask的结果ASK_RESPONSE是否发送
     * 测试ASK_RESPONSE的是否接收并且callback是否执行
     * @param success 执行成功方法还是失败方法
     * @param united
     *
     */
    private void testAskEvent(boolean success, boolean united) {

        String name;
        Map<String, String> paramMap = new HashMap<>();
        if(success) {
            name = united ? UnitedTestEventCallback.SUCCESS_EVENT_NAME : AskTestEventHandler.SUCCESS_EVENT_NAME;
            paramMap.put("param1", "v1");
            paramMap.put("param2", "v2");
        } else {
            name = "玛尔加尼斯";
            paramMap.put("param3", "v3");
            paramMap.put("param4", "v4");
        }

        List<AskEvent> askEvents = ask(united, name, paramMap);

        //判断消息已经发送到kafka
//        askEvents.forEach(this::assertMessageWasSent);

        //判断eventProcess已经创建
        for(AskEvent askEvent : askEvents) {
            EventProcess eventProcess = eventProcessRepository.getByEventId(askEvent.getId());
            assertThat(eventProcess, notNullValue());
//            assertThat(eventProcess.getPayload(), is(eventPublishFromDb.getPayload()));
            assertThat(eventProcess.getStatus(), is(ProcessStatus.NEW));
            assertThat(eventProcess.getEventType(), is(askEvent.getType()));

            assertThat(AskTestEventHandler.events, empty());
            assertThat(RevokableAskTestEventHandler.events, empty());
        }

        //处理askEvent事件
        handleEvent();

        //判断eventProcess已经被处理
        for(AskEvent askEvent : askEvents) {
            EventProcess eventProcess = eventProcessRepository.getByEventId(askEvent.getId());
            assertThat(eventProcess.getStatus(), is(ProcessStatus.PROCESSED));
            //判断事件已经处理
            if(united) {
                if (askEvent.getType().equals(AskTestEvent.EVENT_TYPE)) {
                    assertThat(AskTestEventHandler.events, hasItem((AskTestEvent) askEvent));
                } else if (askEvent.getType().equals(RevokableAskTestEvent.EVENT_TYPE)) {
                    assertThat(RevokableAskTestEventHandler.events, hasItem((RevokableAskTestEvent) askEvent));
                } else {
                    throw new AssertionError("unknown event type :" + askEvent.getType());
                }
            } else {
                assertThat(AskTestEventHandler.events, hasItem((AskTestEvent) askEvent));
            }
        }

        //发送askResponse响应
        sendEvent();
        //处理ASK_RESPONSE事件
        handleEvent();

        //判断对应的回调函数已经被调用
        List<CallbackParam> callbackParams;
        if(united) {
            callbackParams = success ? UnitedTestEventCallback.successParams
                    : UnitedTestEventCallback.failureParams;
        } else {
            callbackParams = success ? AskTestEventSecondCallback.successParams
                    : AskTestEventSecondCallback.failureParams;
        }
        assertThat(callbackParams.size(), is(1));
        CallbackParam callbackParam = callbackParams.get(0);
        assertThat(callbackParam.getAskEvents().size(), is(askEvents.size()));
        assertThat(callbackParam.getParams(), is(paramMap));
        if(!success) {
            FailureInfo failureInfo = callbackParam.getFailureInfo();
            assertThat(failureInfo, notNullValue());
            assertThat(failureInfo.getReason(), is(FailureReason.FAILED));
            assertThat(failureInfo.getFailureTime(), notNullValue());
        }

    }


    /**
     * 测试askEvent超时的处理
     */
    @Test
    public void sendAskEventThenTimeout() throws InterruptedException {

        long ttl = 5000L;

        AskTestEvent askTestEvent = new AskTestEvent("AskTestEvent");
        RevokableAskTestEvent revokableAskTestEvent = new RevokableAskTestEvent("RevokableAskTestEvent");
        List<AskEvent> askEvents = Lists.newArrayList(askTestEvent, revokableAskTestEvent);
        AskParameter askParameter = AskParameterBuilder.askUnited(askTestEvent, revokableAskTestEvent)
                .callbackClass(UnitedTestEventCallback.class).ttl(ttl).build();

        List<AskRequestEventPublish> askRequestEventPublishList = eventBus.ask(askParameter);
        askRequestEventPublishList.stream().forEach(askRequestEventPublish -> {
            askRequestEventPublish.setStatus(ProcessStatus.IGNORE);
            askRequestEventPublishRepository.save(askRequestEventPublish);
        });

        Thread.sleep(ttl + 1000L);

        List<EventWatch> timeoutEventWatchList = eventWatchService.findTimeoutEventWatch(LocalDateTime.now());
        assertThat(timeoutEventWatchList.size(), is(1));

        eventBus.handleTimeoutEventWatch();
        handleEvent();

        timeoutEventWatchList = eventWatchService.findTimeoutEventWatch(LocalDateTime.now());
        assertThat(timeoutEventWatchList.size(), is(0));

        //判断对应的回调函数已经被调用
        List<CallbackParam> callbackParams = UnitedTestEventCallback.failureParams;
        assertThat(callbackParams.size(), is(1));
        CallbackParam callbackParam = callbackParams.get(0);
        assertThat(callbackParam.getAskEvents().size(), is(askEvents.size()));
        FailureInfo failureInfo = callbackParam.getFailureInfo();
        assertThat(failureInfo, notNullValue());
        assertThat(failureInfo.getReason(), is(FailureReason.TIMEOUT));
        assertThat(failureInfo.getFailureTime(), notNullValue());

    }



    private List<AskEvent> ask(boolean united, String name, Map<String, String> paramMap) {
        List<AskEvent> askEvents;
        AskParameter askParameter;
        if(united) {
            AskTestEvent askTestEvent = new AskTestEvent(name);
            RevokableAskTestEvent revokableAskTestEvent = new RevokableAskTestEvent(name);
            askEvents = Lists.newArrayList(askTestEvent, revokableAskTestEvent);
            askParameter = AskParameterBuilder.askUnited(askTestEvent, revokableAskTestEvent)
                    .addParamMap(paramMap)
                    .callbackClass(UnitedTestEventCallback.class).build();
        } else {
            AskTestEvent event = new AskTestEvent(name);
            askEvents = Lists.newArrayList(event);
            askParameter = AskParameterBuilder.ask(event)
                    .addParamMap(paramMap)
                    .callbackClass(AskTestEventSecondCallback.class).build();
        }

        List<AskRequestEventPublish> askRequestEventPublishList = eventBus.ask(askParameter);

        assertThat(askRequestEventPublishList.size(), is(askEvents.size()));
        for(AskRequestEventPublish askRequestEventPublish : askRequestEventPublishList) {
            AskRequestEventPublish eventPublishFromDb = askRequestEventPublishRepository.findOne(askRequestEventPublish.getId());
            assertThat(eventPublishFromDb.getPayload(), is(askRequestEventPublish.getPayload()));
            assertThat(eventPublishFromDb.getStatus(), is(ProcessStatus.NEW));

            boolean result = askEvents.stream()
                    .anyMatch(askEvent -> askEvent.getType().equals(eventPublishFromDb.getEventType()));
            assertThat(result, is(true));
            result = askEvents.stream()
                    .filter(askEvent -> askEvent.getType().equals(eventPublishFromDb.getEventType()))
                    .anyMatch(askEvent -> askEvent.getId().equals(eventPublishFromDb.getEventId()));
            assertThat(result, is(true));
        }

        sendEvent();

        //判断状态已经改过来了
        for(AskRequestEventPublish askRequestEventPublish : askRequestEventPublishList) {
            AskRequestEventPublish eventPublishFromDb = askRequestEventPublishRepository.findOne(askRequestEventPublish.getId());
            assertThat(eventPublishFromDb.getStatus(), is(ProcessStatus.PROCESSED));
        }
        return askEvents;
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
