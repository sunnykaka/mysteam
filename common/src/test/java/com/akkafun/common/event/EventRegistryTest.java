package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/4/11.
 */
public class EventRegistryTest {

    @Test
    public void testInitEventClassMapAndHandlerMapSuccess() throws Exception {

        EventRegistry eventRegistry = new EventRegistry();
        eventRegistry.afterPropertiesSet();

        assertThat(eventRegistry.getEventClassByType(EventType.ASK_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.REVOKABLE_ASK_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.NOTIFY_FIRST_TEST_EVENT), notNullValue());
        assertThat(eventRegistry.getEventClassByType(EventType.NOTIFY_SECOND_TEST_EVENT), notNullValue());

        assertThat(eventRegistry.getNotifyEventHandlers(EventType.NOTIFY_FIRST_TEST_EVENT).size(), is(2));
        assertThat(eventRegistry.getNotifyEventHandlers(EventType.NOTIFY_SECOND_TEST_EVENT).size(), is(1));
        assertThat(eventRegistry.getAskEventHandlers(EventType.ASK_TEST_EVENT).size(), is(1));
        assertThat(eventRegistry.getRevokableAskEventHandlers(EventType.REVOKABLE_ASK_TEST_EVENT).size(), is(1));

    }
//
//    public static class Handler1 {
//
//        @Subscribe
//        public void handleFirstEvent(TestEventFirst event) {
//
//        }
//
//        @Subscribe
//        public void handleFirstEventAgain(TestEventFirst event) {
//
//        }
//
//        @Subscribe
//        public void handleSecondEvent(TestEventSecond event) {
//
//        }
//
//    }
//
//    public static class Handler2 {
//
//        @Subscribe
//        public void handleSecondEvent(TestEventSecond event) {
//
//        }
//
//        @Subscribe
//        public void handleFirstEventThird(TestEventFirst event) {
//
//        }
//    }


}
