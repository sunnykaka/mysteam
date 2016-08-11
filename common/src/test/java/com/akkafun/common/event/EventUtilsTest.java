package com.akkafun.common.event;

import com.akkafun.common.test.domain.NotifyFirstTestEvent;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by liubin on 2016/4/11.
 */
public class EventUtilsTest {


    @Test
    public void testSerialize() {

        NotifyFirstTestEvent testEventFirst = new NotifyFirstTestEvent("张三", LocalDateTime.now());

        String json = EventUtils.serializeEvent(testEventFirst);

        NotifyFirstTestEvent testEventFirstFromJson = EventUtils.deserializeEvent(json, NotifyFirstTestEvent.class);

        assertThat(testEventFirstFromJson, is(testEventFirst));


    }

}
