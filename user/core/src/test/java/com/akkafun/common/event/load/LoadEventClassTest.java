package com.akkafun.common.event.load;

import com.akkafun.common.spring.utils.InnerClassPathScanningCandidateComponentProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/5/30.
 */
public class LoadEventClassTest {

    @Test
    @Ignore
    public void test() throws Exception {

        ClassPathScanningCandidateComponentProvider provider = new InnerClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(FakeUnitedAskEventCallback.class));

        Set<BeanDefinition> componentSet = provider.findCandidateComponents("com/akkafun");

        assertThat(componentSet, not(empty()));
        System.out.println(String.format("FakeUnitedAskEventCallback subclass count: %d, content: %s",
                componentSet.size(), componentSet.toString()));

        ArrayList<BeanDefinition> components = new ArrayList<>(componentSet);
        BeanDefinition beanDefinition = components.get(0);
        Class<? extends FakeUnitedAskEventCallback> callbackClass =
                (Class<? extends FakeUnitedAskEventCallback>) Class.forName(beanDefinition.getBeanClassName());
        Method onSuccessMethod = callbackClass.getMethod("onSuccess", FakeAskEvent[].class);
        assertThat(onSuccessMethod, notNullValue());
        Constructor[] ctors = callbackClass.getDeclaredConstructors();
        for (Constructor cc : ctors)
        {
            System.out.println("my ctor is " + cc.toString());
        }

//        onSuccessMethod.invoke(callbackClass.newInstance(), new FakeAskCouponUse(), new FakeAskDeductBalance());


        FakeEventBus eventBus = new FakeEventBus();

        final Integer a = new Integer(2);

        eventBus.askUnited(new FakeUnitedAskEventCallback() {

            @Override
            public void onSuccess(FakeAskEvent[] askEvents) {
                System.out.println(String.format("onSuccess, params: %s, a: %d", Arrays.toString(askEvents), a));
            }

            @Override
            public void onFailure(FakeAskEvent[] askEvents) {
                System.out.println("onFailure: " + Arrays.toString(askEvents));
            }

        }, new FakeAskCouponUse(), new FakeAskDeductBalance());

    }


}
