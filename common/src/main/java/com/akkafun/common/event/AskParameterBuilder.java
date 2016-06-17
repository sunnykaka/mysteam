package com.akkafun.common.event;

import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.common.exception.EventException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liubin on 2016/6/6.
 */
public class AskParameterBuilder {

    private static Logger logger = LoggerFactory.getLogger(AskParameterBuilder.class);

    private boolean united;

    private List<? extends AskEvent> askEvents;

    private Class<?> callbackClass;

    private Map<String, String> extraParams = new HashMap<>();

    private AskParameterBuilder(boolean united, List<? extends AskEvent> askEvents) {
        this.united = united;
        this.askEvents = askEvents;
    }

    public static AskParameterBuilder ask(AskEvent askEvent) {
        Preconditions.checkNotNull(askEvent);
        if(askEvent.getId() != null) {
            throw new EventException("askEvent的ID不为空");
        }
        return new AskParameterBuilder(false, Lists.newArrayList(askEvent));
    }

    public static AskParameterBuilder askUnited(AskEvent askEvent1, AskEvent askEvent2, AskEvent... otherEvents) {
        Preconditions.checkNotNull(askEvent1);
        Preconditions.checkNotNull(askEvent2);
        List<AskEvent> askEvents = Lists.newArrayList(askEvent1, askEvent2);
        if(otherEvents != null && otherEvents.length > 0) {
            askEvents.addAll(Arrays.asList(otherEvents));
        }
        if(askEvents.stream().anyMatch(x -> x.getId() != null)) {
            throw new EventException("askEvent的ID不为空");
        }
        return new AskParameterBuilder(true, askEvents);
    }

    /**
     *
     * 回调类必须要有onSuccess方法, 可以没有onFailure方法.
     * 回调类必须要有无参构造函数, 并且是public作用域.
     * 回调类不能是匿名类, 因为匿名类可能没有无参构造函数
     * 如果回调类不符合要求, 会抛出EventException
     *
     *
     * 非united事件回调类示例:
     * <blockquote><pre>
     * class AskCouponUseCallback {
     *   public void onSuccess(AskCouponUse couponUseEvent) {
     *     //...
     *   }
     *   public void onFailure(AskCouponUse couponUseEven, FailureInfo failureInfo) {
     *     //...
     *   }
     * }
     * </blockquote></pre>
     *
     * united事件回调类示例:
     * <blockquote><pre>
     * class AskCouponUseCallback {
     *   public void onSuccess(AskCouponUse couponUseEvent, AskDeductBalance deductBalanceEvent, Long orderId) {
     *     //...
     *   }
     *
     *   public void onFailure(AskCouponUse couponUseEvent, AskDeductBalance deductBalanceEvent, FailureInfo failureInfo, Long orderId) {
     *     //...
     *   }
     * }
     * </blockquote></pre>
     *
     * @param callbackClass
     * @return
     */
    public AskParameterBuilder callbackClass(Class<?> callbackClass) {
        EventRegistry.getAskEventCallback(callbackClass.getName());
        this.callbackClass = callbackClass;
        return this;
    }

    public AskParameterBuilder addParam(String name, String value) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);
        extraParams.put(name, value);
        return this;
    }

    public AskParameterBuilder addParamMap(Map<String, String> paramMap) {
        paramMap.forEach((k, v) -> {
            Preconditions.checkNotNull(k);
            Preconditions.checkNotNull(v);
        });
        extraParams.putAll(paramMap);
        return this;
    }


    public AskParameter build() {
        return new AskParameter(united, askEvents, callbackClass, extraParams);
    }

}
