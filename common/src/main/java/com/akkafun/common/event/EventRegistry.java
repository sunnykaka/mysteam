package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.*;
import com.akkafun.base.exception.BaseException;
import com.akkafun.common.event.constant.EventCategory;
import com.akkafun.common.event.handler.AskEventHandler;
import com.akkafun.common.event.handler.NotifyEventHandler;
import com.akkafun.common.event.handler.RevokableAskEventHandler;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.spring.utils.InnerClassPathScanningCandidateComponentProvider;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by liubin on 2016/4/12.
 */
public class EventRegistry implements InitializingBean, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(EventRegistry.class);

    public static final String BASE_PACKAGE = "com/akkafun";

    private static final LoadingCache<String, AskEventCallback> askEventCallbackCache =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<String, AskEventCallback>() {
                        @Override
                        public AskEventCallback load(String callbackClassName) throws Exception {
                            return AskEventCallback.createCallback(callbackClassName);
                        }
                    });

    private Map<EventType, Class<? extends BaseEvent>> eventTypeClassMap = new HashMap<>();

    private SetMultimap<EventType, NotifyEventHandler> notifyEventHandlerMap = HashMultimap.create();

    private SetMultimap<EventType, AskEventHandler> askEventHandlerMap = HashMultimap.create();

    private SetMultimap<EventType, RevokableAskEventHandler> revokableAskEventHandlerMap = HashMultimap.create();

    private Set<EventType> interestedEventTypes = new HashSet<>();


    /**
     * 根据回调类名解析成回调对象, 如果回调类不符合要求, 会抛出EventException
     * @param callbackClassName
     * @return
     */
    public static AskEventCallback getAskEventCallback(String callbackClassName) {
        try {
            return askEventCallbackCache.getUnchecked(callbackClassName);
        } catch (UncheckedExecutionException e) {
            throw new EventException(e.getCause());
        }
    }


    /**
     * 1. 查询所有的BaseEvent子类, 将他们的eventType和class绑定.
     * 2. 查找所有NotifyEventHandler, AskEventHandler, RevokableAskEventHandler的实现类, 将他们与对应事件绑定
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {

        ClassPathScanningCandidateComponentProvider provider = new InnerClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(BaseEvent.class));

        Map<EventType, Class<? extends BaseEvent>> map = new HashMap<>();
        //查找所有BaseEvent子类
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(BASE_PACKAGE);
        for(BeanDefinition beanDefinition : beanDefinitions) {
            String eventClassName = beanDefinition.getBeanClassName();
            Class<? extends BaseEvent> eventClass = (Class<? extends BaseEvent>)Class.forName(eventClassName);
            EventType eventType = getEventTypeFromClass(eventClass);

            Class<? extends BaseEvent> previousValue = map.put(eventType, eventClass);
            if(previousValue != null) {
                throw new EventException(String.format("duplicate eventType: %s, eventClass[%s, %s]",
                        eventType, previousValue, eventClass));
            }

        }
        //得到事件类型和事件类class的映射
        synchronized (this) {
            this.eventTypeClassMap = Collections.unmodifiableMap(map);
        }

        //得到事件类型和对应的handler类的映射
        SetMultimap<EventType, NotifyEventHandler> notifyEventHandlerMap = buildHandlerMap(NotifyEventHandler.class);
        SetMultimap<EventType, AskEventHandler> askEventHandlerMap = buildHandlerMap(AskEventHandler.class);
        SetMultimap<EventType, RevokableAskEventHandler> revokableAskEventHandlerMap = buildHandlerMap(RevokableAskEventHandler.class);
        synchronized (this) {
            this.notifyEventHandlerMap = Multimaps.unmodifiableSetMultimap(notifyEventHandlerMap);
            this.askEventHandlerMap = Multimaps.unmodifiableSetMultimap(askEventHandlerMap);
            this.revokableAskEventHandlerMap = Multimaps.unmodifiableSetMultimap(revokableAskEventHandlerMap);
        }

        //得到感兴趣的所有事件类型
        Set<EventType> allInterestedSet = new HashSet<>();
        allInterestedSet.add(AskResponseEvent.EVENT_TYPE);
        allInterestedSet.add(RevokeAskEvent.EVENT_TYPE);
        allInterestedSet.addAll(notifyEventHandlerMap.keySet());
        allInterestedSet.addAll(askEventHandlerMap.keySet());
        allInterestedSet.addAll(revokableAskEventHandlerMap.keySet());

        synchronized (this) {
            this.interestedEventTypes = Collections.unmodifiableSet(allInterestedSet);
        }
    }

    /**
     * 得到感兴趣的所有事件类型
     * @return
     */
    public Set<EventType> getInterestedEventTypes() {
        return this.interestedEventTypes;
    }

    /**
     * 根据EventType得到对应的class
     * @param eventType
     * @return
     */
    public Class<? extends BaseEvent> getEventClassByType(EventType eventType) {
        return eventTypeClassMap.get(eventType);
    }

    /**
     * 根据EventType得到对应的EventCategory
     * @param eventType
     * @return
     */
    public EventCategory getEventCategoryByType(EventType eventType) {
        Class<? extends BaseEvent> eventClass = eventTypeClassMap.get(eventType);
        EventCategory eventCategory;
        if(eventClass.equals(AskResponseEvent.class)) {
            eventCategory = EventCategory.ASKRESP;
        } else if(NotifyEvent.class.isAssignableFrom(eventClass)) {
            eventCategory = EventCategory.NOTIFY;
        } else if(AskEvent.class.isAssignableFrom(eventClass)) {
            eventCategory = EventCategory.ASK;
        } else if(RevokeAskEvent.class.isAssignableFrom(eventClass)) {
            eventCategory = EventCategory.REVOKE;
        } else {
            throw new EventException("unknown event category for event type: " + eventType);
        }

        return eventCategory;
    }

    public BaseEvent deserializeEvent(EventType eventType, String payload) {
        Class<? extends BaseEvent> eventClass = eventTypeClassMap.get(eventType);
        return EventUtils.deserializeEvent(payload, eventClass);
    }

    /**
     * @param payload
     * @return
     */
    public AskResponseEvent deserializeAskResponseEvent(String payload) {
        return EventUtils.deserializeEvent(payload, AskResponseEvent.class);
    }

    /**
     * 事件类型是否实现了Revokable接口
     * @param eventType
     * @return
     */
    public boolean isEventRevokable(EventType eventType) {
        Class<? extends BaseEvent> eventClass = eventTypeClassMap.get(eventType);
        return Revokable.class.isAssignableFrom(eventClass);
    }

    /**
     * 根据事件类型查找notify监听器
     * @param eventType
     * @return
     */
    public Set<NotifyEventHandler> getNotifyEventHandlers(EventType eventType) {
        return notifyEventHandlerMap.get(eventType);
    }

    /**
     * 根据事件类型查找ask监听器
     * @param eventType
     * @return
     */
    public Set<AskEventHandler> getAskEventHandlers(EventType eventType) {
        return askEventHandlerMap.get(eventType);
    }

    /**
     * 根据事件类型查找revoke监听器
     * @param eventType
     * @return
     */
    public Set<RevokableAskEventHandler> getRevokableAskEventHandlers(EventType eventType) {
        return revokableAskEventHandlerMap.get(eventType);
    }


    /**
     * 解析handler类
     * @param handlerClass
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private <T> SetMultimap<EventType, T> buildHandlerMap(Class<T> handlerClass) throws Exception{

        SetMultimap<EventType, T> multimap = HashMultimap.create();

        ClassPathScanningCandidateComponentProvider provider = new InnerClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(handlerClass));

        //查询特定handler类所有子类
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(BASE_PACKAGE);
        for(BeanDefinition beanDefinition : beanDefinitions) {
            String className = beanDefinition.getBeanClassName();
            Class<? extends T> eventHandlerClass = (Class<? extends T>)Class.forName(className);
            //得到类型参数
            Type type = eventHandlerClass.getGenericInterfaces()[0];
            if (!(type instanceof ParameterizedType)) {
                throw new BaseException(String.format("class %s type parameter is not instance of ParameterizedType," +
                        " the type is %s ", eventHandlerClass, type.toString()));
            }
            //得到类型参数对应的事件类
            Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
            String eventClassName = actualType.getTypeName();
            Class<? extends BaseEvent> eventClass = (Class<? extends BaseEvent>)Class.forName(eventClassName);
            EventType eventType = getEventTypeFromClass(eventClass);

            multimap.put(eventType, eventHandlerClass.newInstance());
        }

        return multimap;
    }

    private EventType getEventTypeFromClass(Class<? extends BaseEvent> eventClass) {
        Field eventTypeField = FieldUtils.getField(eventClass, "EVENT_TYPE");
        EventType eventType;
        if(eventTypeField == null) {
            throw new BaseException("event class " + eventClass
                    + " require a public static field EVENT_TYPE ");
        }
        try {
            eventType = (EventType)eventTypeField.get(null);
            Preconditions.checkNotNull(eventType);
        } catch (IllegalAccessException e) {
            logger.error("", e);
            throw new BaseException("event class " + eventClass
                    + " require a static field EVENT_TYPE ");
        }
        return eventType;
    }

    @Override
    public void destroy() throws Exception {
        synchronized (this) {
            this.eventTypeClassMap = new HashMap<>();
            this.notifyEventHandlerMap = HashMultimap.create();
            this.askEventHandlerMap = HashMultimap.create();
            this.revokableAskEventHandlerMap = HashMultimap.create();
        }
    }

}
