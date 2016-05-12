package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.common.event.constants.TestEventFirst;
import com.akkafun.common.event.constants.TestEventSecond;
import com.akkafun.common.exception.EventException;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/4/11.
 */
public class EventRegistryTest {

    @Test
    public void test() {

        EventRegistry eventRegistry = EventRegistry.getInstance();
        eventRegistry.clear();
        eventRegistry.register(new Handler1());
        eventRegistry.register(new Handler2());

        boolean pass = false;
        try {
            eventRegistry.getAllEventType();
        } catch (EventException expected) {
            pass = true;
        }
        if(!pass) {
            throw new AssertionError("在eventRegistry.getAllEventType() 预期抛出EventException, 但是并没有");
        }

        eventRegistry.completeRegister();

        Set<EventType> eventTypeSet = eventRegistry.getAllEventType();
        assertThat(eventTypeSet.size(), is(2));
        assertThat(eventTypeSet, containsInAnyOrder(EventType.TEST_EVENT_FIRST, EventType.TEST_EVENT_SECOND));

        Set<EventSubscriber> eventFirstSubscriberSet = eventRegistry.findEventSubscriberByType(EventType.TEST_EVENT_FIRST);
        Set<EventSubscriber> eventSecondSubscriberSet = eventRegistry.findEventSubscriberByType(EventType.TEST_EVENT_SECOND);
        assertThat(eventFirstSubscriberSet.size(), is(3));
        assertThat(eventSecondSubscriberSet.size(), is(2));

    }

    public static class Handler1 {

        @Subscribe
        public void handleFirstEvent(TestEventFirst event) {

        }

        @Subscribe
        public void handleFirstEventAgain(TestEventFirst event) {

        }

        @Subscribe
        public void handleSecondEvent(TestEventSecond event) {

        }

    }

    public static class Handler2 {

        @Subscribe
        public void handleSecondEvent(TestEventSecond event) {

        }

        @Subscribe
        public void handleFirstEventThird(TestEventFirst event) {

        }
    }


}
