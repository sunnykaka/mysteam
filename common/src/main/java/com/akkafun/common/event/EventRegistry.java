package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.spring.utils.InnerClassPathScanningCandidateComponentProvider;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.kafka.common.utils.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by liubin on 2016/4/12.
 */
public class EventRegistry implements InitializingBean, DisposableBean {

    private static final EventRegistry INSTANCE = new EventRegistry();

    public static EventRegistry getInstance() {
        return INSTANCE;
    }

    private EventRegistry() {}

    private static Logger logger = LoggerFactory.getLogger(EventRegistry.class);

    private volatile boolean complete = false;

    private final SetMultimap<EventType, EventSubscriber> subscribersByType =
            HashMultimap.create();

    /**
     * A thread-safe cache that contains the mapping from each class to all methods in that class,
     * that are annotated with {@code @Subscribe}. The cache is shared across all
     * instances of this class; this greatly improves performance if multiple EventBus instances are
     * created and objects of the same class are registered on all of them.
     */
    private static final LoadingCache<Class<?>, ImmutableList<Method>> subscriberMethodsCache =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<Class<?>, ImmutableList<Method>>() {
                        @Override
                        public ImmutableList<Method> load(Class<?> concreteClass) throws Exception {
                            return getAnnotatedMethodsInternal(concreteClass);
                        }
                    });


    /**
     * 调用register进行监听器的注册
     * @param listener
     */
    public synchronized void register(Object listener) {

        if(complete) {
            throw new EventException("事件注册已经完成, 不能再进行注册了");
        }

        Multimap<EventType, EventSubscriber> methodsInListener = HashMultimap.create();
        Class<?> clazz = listener.getClass();
        for (Method method : getAnnotatedMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<? extends BaseEvent> eventClass = (Class<? extends BaseEvent>) parameterTypes[0];
            //TODO cache here
            Field evenTypeField = FieldUtils.getField(eventClass, "EVENT_TYPE");
            if(evenTypeField == null) {
                throw new IllegalArgumentException("event class " + eventClass
                        + " require a public static field EVENT_TYPE ");
            }
            EventType eventType;
            try {
                eventType = (EventType)evenTypeField.get(null);
            } catch (IllegalAccessException e) {
                logger.error("", e);
                throw new IllegalArgumentException("event class " + eventClass
                        + " require a static field EVENT_TYPE ");
            }

            EventSubscriber subscriber = new EventSubscriber(listener, method, eventClass);
            methodsInListener.put(eventType, subscriber);
        }

        subscribersByType.putAll(methodsInListener);
    }

    /**
     * 根据事件类型查找所有的监听器
     * @param eventType
     * @return
     */
    public Set<EventSubscriber> findEventSubscriberByType(EventType eventType) {
        if(!complete) {
            throw new EventException("请先调用EventRegistry.registerComplete完成事件注册");
        }

        return subscribersByType.get(eventType);
    }

    /**
     * 得到已注册的所有事件类型
     * @return
     */
    public Set<EventType> getAllEventType() {
        if(!complete) {
            throw new EventException("请先调用EventRegistry.registerComplete完成事件注册");
        }

        return subscribersByType.keySet();
    }

    /**
     * 监听器全部注册之后, 需要调用该方法完成注册.
     * 防止已经完成注册之后又进行注册
     */
    public void completeRegister() {
        complete = true;
    }

    public synchronized void clear() {

        logger.info("EventRegistry clear...");
        subscribersByType.clear();
        complete = false;

    }

    private static ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
        try {
            return subscriberMethodsCache.getUnchecked(clazz);
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    private static ImmutableList<Method> getAnnotatedMethodsInternal(Class<?> clazz) {
        Map<MethodIdentifier, Method> identifiers = Maps.newHashMap();
        for (Method clazzMethod : clazz.getMethods()) {
            if (clazzMethod.isAnnotationPresent(Subscribe.class)
                    && !clazzMethod.isBridge()) {
                Class<?>[] parameterTypes = clazzMethod.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + clazzMethod
                            + " has @Subscribe annotation, but requires " + parameterTypes.length
                            + " arguments.  Event subscriber methods must require a single argument.");
                }
                Class<?> parameterType = parameterTypes[0];
                if(!BaseEvent.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("Method " + clazzMethod
                            + " has @Subscribe annotation, but first argument type is " + parameterType.getName()
                            + ".  Event subscriber methods must require a BaseEvent argument.");
                }

                MethodIdentifier ident = new MethodIdentifier(clazzMethod);
                if (!identifiers.containsKey(ident)) {
                    identifiers.put(ident, clazzMethod);
                }
            }
        }
        return ImmutableList.copyOf(identifiers.values());
    }


    private Map<EventType, Class<? extends BaseEvent>> eventTypeClassMap = new CopyOnWriteMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 1. 查询所有的BaseEvent子类, 将他们的eventType和class绑定.
        // 2. 查找所有NotifyEventHandler, AskEventHandler, RevokableAskEventHandler的实现类, 将他们与对应事件绑定

        ClassPathScanningCandidateComponentProvider provider = new InnerClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(BaseEvent.class));

        Set<BeanDefinition> componentSet = provider.findCandidateComponents("com/akkafun");



    }

    @Override
    public void destroy() throws Exception {

    }


    private static final class MethodIdentifier {
        private final String name;
        private final List<Class<? extends BaseEvent>> parameterTypes;

        MethodIdentifier(Method method) {
            this.name = method.getName();
            this.parameterTypes = Arrays.asList((Class<? extends BaseEvent>[]) method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, parameterTypes);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MethodIdentifier) {
                MethodIdentifier ident = (MethodIdentifier) o;
                return name.equals(ident.name) && parameterTypes.equals(ident.parameterTypes);
            }
            return false;
        }
    }


}
