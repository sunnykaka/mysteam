package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.utils.JsonUtils;
import com.akkafun.common.test.callbacks.AskTestEventFirstCallback;
import com.akkafun.common.test.callbacks.AskTestEventSecondCallback;
import com.akkafun.common.test.callbacks.CallbackParam;
import com.akkafun.common.event.domain.AskRequestEventPublish;
import com.akkafun.common.test.domain.AskTestEvent;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/4/11.
 */
public class EventRegistryTest {

    EventRegistry eventRegistry = new EventRegistry();

    @Before
    public void init() throws Exception {
        EventTestUtils.clear();
        eventRegistry.afterPropertiesSet();
    }

    @After
    public void destroy() throws Exception {
        eventRegistry.destroy();
    }


    @Test
    public void testInitEventClassMapAndHandlerMapSuccess() throws Exception {

        assertThat(eventRegistry.getEventClassByType(EventType.ASK_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.REVOKABLE_ASK_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.NOTIFY_FIRST_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.NOTIFY_SECOND_TEST_EVENT), notNullValue());

        assertThat(eventRegistry.getNotifyEventHandlers(EventType.NOTIFY_FIRST_TEST_EVENT).size(), is(2));
        assertThat(eventRegistry.getNotifyEventHandlers(EventType.NOTIFY_SECOND_TEST_EVENT).size(), is(1));
        assertThat(eventRegistry.getAskEventHandlers(EventType.ASK_TEST_EVENT).size(), is(1));
        assertThat(eventRegistry.getRevokableAskEventHandlers(EventType.REVOKABLE_ASK_TEST_EVENT).size(), is(1));

    }


    @Test
    public void testAllInterestedEventType() throws Exception {

        assertThat(eventRegistry.getInterestedEventTypes(),
                hasItems(EventType.ASK_TEST_EVENT, EventType.REVOKABLE_ASK_TEST_EVENT,
                        EventType.NOTIFY_FIRST_TEST_EVENT, EventType.NOTIFY_SECOND_TEST_EVENT));


        assertThat(eventRegistry.isEventRevokable(EventType.REVOKABLE_ASK_TEST_EVENT), is(true));
        assertThat(eventRegistry.isEventRevokable(EventType.ASK_TEST_EVENT), is(false));
    }


    @Test
    public void testReadAndCallCallbackSuccess(){

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("param1", "v1");
        paramMap.put("param2", "v2");
        String jsonParam = JsonUtils.object2Json(paramMap);

        AskEventCallback callback = EventRegistry.getAskEventCallback(
                AskTestEventFirstCallback.class.getName());
        assertThat(callback.getCallbackClass().equals(AskTestEventFirstCallback.class), is(true));
        assertThat(callback.getSuccessMethod(), notNullValue());
        assertThat(callback.getFailureMethod().isPresent(), is(false));
        assertThat(callback.getSuccessParameters().size(), is(1));
        assertThat(callback.getFailureParameters().size(), is(0));

        AskTestEvent askTestEvent = new AskTestEvent(RandomStringUtils.randomAlphabetic(6));
        AskRequestEventPublish askRequestEventPublish = new AskRequestEventPublish();
        askRequestEventPublish.setEventType(AskTestEvent.EVENT_TYPE);
        askRequestEventPublish.setPayload(EventUtils.serializeEvent(askTestEvent));

        //调用AskTestEventFirstCallback.onSuccess方法
        callback.call(eventRegistry, true, Lists.newArrayList(askRequestEventPublish), null, null);
        assertThat(AskTestEventFirstCallback.successParams.size(), is(1));
        CallbackParam callbackParam = AskTestEventFirstCallback.successParams.get(0);
        assertThat(callbackParam.getAskEvents().size(), is(1));
        assertThat(callbackParam.getAskEvents().get(0), is(askTestEvent));

        callback = EventRegistry.getAskEventCallback(AskTestEventSecondCallback.class.getName());
        assertThat(callback.getCallbackClass().equals(AskTestEventSecondCallback.class), is(true));
        assertThat(callback.getSuccessMethod(), notNullValue());
        assertThat(callback.getFailureMethod().isPresent(), is(true));
        assertThat(callback.getSuccessParameters().size(), is(3));
        assertThat(callback.getFailureParameters().size(), is(4));

        //调用AskTestEventSecondCallback.onSuccess方法
        callback.call(eventRegistry, true, Lists.newArrayList(askRequestEventPublish), jsonParam, null);
        assertThat(AskTestEventSecondCallback.successParams.size(), is(1));
        assertThat(AskTestEventSecondCallback.failureParams.size(), is(0));
        callbackParam = AskTestEventSecondCallback.successParams.get(0);
        assertThat(callbackParam.getAskEvents().size(), is(1));
        assertThat(callbackParam.getAskEvents().get(0), is(askTestEvent));
        assertThat(callbackParam.getParams().size(), is(2));
        assertThat(callbackParam.getParams(), hasEntry("param1", "v1"));
        assertThat(callbackParam.getParams(), hasEntry("param2", "v2"));

        //调用AskTestEventSecondCallback.onFailure方法
        callback.call(eventRegistry, false, Lists.newArrayList(askRequestEventPublish), null, null);
        assertThat(AskTestEventSecondCallback.successParams.size(), is(1));
        assertThat(AskTestEventSecondCallback.failureParams.size(), is(1));

    }

}
