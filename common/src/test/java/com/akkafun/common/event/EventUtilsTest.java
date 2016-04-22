package com.akkafun.common.event;

import com.akkafun.common.event.EventUtils;
import com.akkafun.common.event.constants.TestEventFirst;
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

        TestEventFirst testEventFirst = new TestEventFirst("张三", LocalDateTime.now());

        String json = EventUtils.serializeEvent(testEventFirst);

        TestEventFirst testEventFirstFromJson = EventUtils.deserializeEvent(json, TestEventFirst.class);

        assertThat(testEventFirstFromJson, is(testEventFirst));


    }

}
