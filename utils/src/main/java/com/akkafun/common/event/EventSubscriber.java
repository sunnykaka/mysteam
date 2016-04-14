package com.akkafun.common.event;


import static com.google.common.base.Preconditions.checkNotNull;

import com.akkafun.base.event.domain.BaseEvent;
import com.google.common.base.Preconditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class EventSubscriber {

    /**
     * Object sporting the subscriber method.
     */
    private final Object target;
    /**
     * Subscriber method.
     */
    private final Method method;

    private final Class<? extends BaseEvent> eventClass;

    /**
     * Creates a new EventSubscriber to wrap {@code method} on @{code target}.
     *
     * @param target object to which the method applies.
     * @param method subscriber method.
     */
    EventSubscriber(Object target, Method method, Class<? extends BaseEvent> eventClass) {
        Preconditions.checkNotNull(target,
                "EventSubscriber target cannot be null.");
        Preconditions.checkNotNull(method, "EventSubscriber method cannot be null.");

        this.target = target;
        this.method = method;
        this.eventClass = eventClass;
        method.setAccessible(true);
    }

    /**
     * Invokes the wrapped subscriber method to handle {@code event}.
     *
     * @param event event to handle
     * @throws InvocationTargetException if the wrapped method throws any
     *                                   {@link Throwable} that is not an {@link Error} ({@code Error} instances are
     *                                   propagated as-is).
     */
    public void handleEvent(BaseEvent event) throws InvocationTargetException {
        checkNotNull(event);
        try {
            method.invoke(target, event);
        } catch (IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new Error("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return "[wrapper " + method + "]";
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        return (PRIME + method.hashCode()) * PRIME
                + System.identityHashCode(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventSubscriber) {
            EventSubscriber that = (EventSubscriber) obj;
            // Use == so that different equal instances will still receive events.
            // We only guard against the case that the same object is registered
            // multiple times
            return target == that.target && method.equals(that.method);
        }
        return false;
    }

    public Object getSubscriber() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends BaseEvent> getEventClass() {
        return eventClass;
    }
}
