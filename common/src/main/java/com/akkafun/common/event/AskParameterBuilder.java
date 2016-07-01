package com.akkafun.common.event;

import com.akkafun.base.Constants;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.common.exception.EventException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * Created by liubin on 2016/6/6.
 */
public class AskParameterBuilder {

    private static Logger logger = LoggerFactory.getLogger(AskParameterBuilder.class);

    private boolean united;

    private List<? extends AskEvent> askEvents;

    private Class<?> callbackClass;

    private Map<String, String> extraParams = new HashMap<>();

    private Optional<LocalDateTime> timeoutTime = Optional.empty();

    private AskParameterBuilder(boolean united, List<? extends AskEvent> askEvents) {
        this.timeoutTime = Optional.of(LocalDateTime.now()
                .plus(Constants.ASK_TIMEOUT, ChronoField.MILLI_OF_DAY.getBaseUnit()));
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

    public static AskParameterBuilder askOptional(Optional<? extends AskEvent>... optionals) {
        Preconditions.checkNotNull(optionals);
        Preconditions.checkArgument(optionals.length > 0);
        List<AskEvent> askEvents = new ArrayList<>();
        for(Optional<? extends AskEvent> optional : optionals) {
            optional.ifPresent(askEvents::add);
        }
        Preconditions.checkArgument(!askEvents.isEmpty());

        if(askEvents.size() == 1) {
            return ask(askEvents.get(0));
        } else if(askEvents.size() == 2) {
            return askUnited(askEvents.get(0), askEvents.get(1));
        } else {
            List<AskEvent> events = askEvents.subList(2, askEvents.size());
            return askUnited(askEvents.get(0), askEvents.get(1), events.toArray(new AskEvent[events.size()]));
        }
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

    public AskParameterBuilder ttl(long ttl) {
        if(ttl > 0L) {
            LocalDateTime timeoutTime = LocalDateTime.now()
                    .plus(ttl, ChronoField.MILLI_OF_DAY.getBaseUnit());
            this.timeoutTime = Optional.of(timeoutTime);
        } else {
            this.timeoutTime = Optional.empty();
        }
        return this;
    }



    public AskParameter build() {

        Preconditions.checkNotNull(callbackClass);
        Preconditions.checkNotNull(askEvents);
        AskEventCallback askEventCallback = EventRegistry.getAskEventCallback(callbackClass.getName());
        askEventCallback.checkMethodParameter(united, askEvents);

        return new AskParameter(united, askEvents, callbackClass, extraParams, timeoutTime);
    }

}
