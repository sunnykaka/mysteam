package com.akkafun.common.event;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.domain.AskEvent;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.utils.JsonUtils;
import com.akkafun.common.event.domain.AskRequestEventPublish;
import com.akkafun.common.exception.EventException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AskEventCallback {

    private static Logger logger = LoggerFactory.getLogger(AskEventCallback.class);

    private final String callbackClassName;
    private final Class<?> callbackClass;
    private final Method successMethod;
    private final List<Parameter> successParameters;
    private final Optional<Method> failureMethod;
    private final List<Parameter> failureParameters;

    private AskEventCallback(String callbackClassName, Class<?> callbackClass, Method successMethod,
                            Optional<Method> failureMethod, List<Parameter> successParameters,
                            List<Parameter> failureParameters) {
        this.callbackClassName = callbackClassName;
        this.callbackClass = callbackClass;
        this.successMethod = successMethod;
        this.successParameters = successParameters;
        this.failureMethod = failureMethod;
        this.failureParameters = failureParameters;
    }

    /**
     * 根据className找到对应的类, 读取类的信息
     * @param callbackClassName
     * @return
     * @throws Exception
     */
    public static AskEventCallback createCallback(String callbackClassName) throws Exception {

        Class<?> callbackClass = Class.forName(callbackClassName);

        List<Method> methods = Arrays.asList(callbackClass.getMethods());
        Optional<Method> successMethodOptional = getCallbackMethod(callbackClassName, methods, true);
        Optional<Method> failureMethodOptional = getCallbackMethod(callbackClassName, methods, false);
        if(!successMethodOptional.isPresent()) {
            throw new EventException(String.format("回调类%s中没有%s方法",
                    callbackClassName, EventUtils.SUCCESS_CALLBACK_NAME));
        }

        List<Parameter> successParameters = Arrays.asList(successMethodOptional.get().getParameters());
        checkCallbackParameters(callbackClassName, successParameters);

        List<Parameter> failureParameters = new ArrayList<>();
        if(failureMethodOptional.isPresent()) {
            failureParameters = Arrays.asList(failureMethodOptional.get().getParameters());
            checkCallbackParameters(callbackClassName, failureParameters);
        }

        return new AskEventCallback(callbackClassName, callbackClass, successMethodOptional.get(),
                failureMethodOptional, successParameters, failureParameters);

    }

    private static void checkCallbackParameters(String callbackClassName, List<Parameter> parameters) {
        parameters.stream().map(Parameter::getType).forEach(parameterType -> {
            if(!BaseEvent.class.isAssignableFrom(parameterType) && !parameterType.equals(FailureInfo.class)
                    && !parameterType.equals(String.class)) {
                throw new EventException(String.format("回调方法参数类型必须是String, " +
                                "FailureInfo或者BaseEvent的子类, 实际类型: %s, 类名: %s",
                        parameterType, callbackClassName));
            }
        });
    }

    /**
     * 查找回调成功或失败的方法
     * @param callbackClassName
     * @param methods
     * @param success
     * @return
     * @throws Exception
     */
    private static Optional<Method> getCallbackMethod(String callbackClassName, List<Method> methods,
                                                      boolean success) throws Exception {

        String methodName = EventUtils.getAskCallbackMethodName(success);
        List<Method> targetMethods = methods.stream()
                .filter(method -> methodName.equals(method.getName()))
                .collect(Collectors.toList());
        if(targetMethods.size() > 1) {
            throw new EventException(String.format("回调类%s有%d个%s方法, 应该只能有1个",
                    callbackClassName, targetMethods.size(), methodName));
        }
        return targetMethods.isEmpty() ? Optional.empty() : Optional.of(targetMethods.get(0));
    }

    public String getCallbackClassName() {
        return callbackClassName;
    }

    public Class<?> getCallbackClass() {
        return callbackClass;
    }

    public Method getSuccessMethod() {
        return successMethod;
    }

    public Optional<Method> getFailureMethod() {
        return failureMethod;
    }

    public List<Parameter> getSuccessParameters() {
        return successParameters;
    }

    public List<Parameter> getFailureParameters() {
        return failureParameters;
    }

    /**
     * 执行回调函数
     * @param eventRegistry
     * @param success
     * @param askEvents
     * @param extraParams
     * @param failureInfo
     */
    public void call(EventRegistry eventRegistry, boolean success, List<AskRequestEventPublish> askEvents,
                     String extraParams, FailureInfo failureInfo) {

        if(!success && !failureMethod.isPresent()) {
            //没有失败的回调方法, 直接返回
            return;
        }

        if(StringUtils.isBlank(extraParams)) {
            extraParams = "{}";
        }
        final Map<String, String> extraParamMap = JsonUtils.json2Object(extraParams, Map.class);

        List<Parameter> parameters = success ? successParameters : failureParameters;
        Method method = success ? successMethod : failureMethod.get();

        Map<Class<?>, BaseEvent> askEventMap = askEvents.stream()
                .map(x -> eventRegistry.deserializeEvent(x.getEventType(), x.getPayload()))
                .collect(Collectors.toMap(x -> x.getClass(), Function.identity()));

        List<Object> invokeMethodParameters = parameters.stream()
                .map(p -> {
                    Class<?> parameterType = p.getType();
                    if(BaseEvent.class.isAssignableFrom(parameterType)) {
                        //AskEvent类型的参数
                        return askEventMap.get(parameterType);
                    } else if(parameterType.equals(FailureInfo.class)) {
                        //FailureInfo类型的参数
                        return failureInfo;
                    } else if(parameterType.equals(String.class)){
                        //extraParams参数
                        return extraParamMap.get(p.getName());
                    } else {
                        throw new EventException(String.format("回调方法参数类型必须是String, " +
                                        "FailureInfo或者BaseEvent的子类, 实际类型: %s, 类名: %s",
                                parameterType, callbackClassName));
                    }
                })
                .collect(Collectors.toList());

        try {
            if(logger.isDebugEnabled()) {
                logger.debug(String.format("invoke callback: %s, method: %s, params: %s", callbackClassName,
                        EventUtils.getAskCallbackMethodName(success), invokeMethodParameters));
            }
            method.invoke(callbackClass.newInstance(), invokeMethodParameters.toArray());

        } catch (IllegalAccessException | InstantiationException e) {
            throw new EventException(e);
        } catch (InvocationTargetException e) {
            if(e.getTargetException() instanceof EventException) {
                throw (EventException)e.getTargetException();
            } else {
                throw new EventException(e.getTargetException());
            }
        }
    }

    /**
     * 校验回调方法参数和实际传的值是否匹配
     * @param united
     * @param askEvents
     */
    public void checkMethodParameter(boolean united, List<? extends AskEvent> askEvents) {

        if(!united && askEvents.size() != 1) {
            throw new EventException("ask请求不是united但是askEvent数量不等于1");
        } else if(united && askEvents.size() <= 1){
            throw new EventException("ask请求是united但是askEvent数量小于等于1");
        }

        checkParameterType(true);
        checkParameterType(false);

        checkAskEventParameter(true, askEvents);
        checkAskEventParameter(false, askEvents);
    }

    /**
     * 校验方法参数类型
     * @param success
     */
    private void checkParameterType(boolean success) {
        List<Parameter> parameters = success ? successParameters : failureParameters;

        boolean allParameterValid;
        allParameterValid = parameters.stream()
                .map(Parameter::getType)
                .allMatch(clazz -> BaseEvent.class.isAssignableFrom(clazz) || clazz.equals(String.class)
                        || clazz.equals(FailureInfo.class));
        if(!allParameterValid) {
            throw new EventException(String.format("回调类%s的%s方法参数类型必须是String, FailureInfo或者BaseEvent的子类",
                    callbackClassName, EventUtils.getAskCallbackMethodName(success)));
        }

    }

    /**
     * 校验方法askEvent参数声明与实际是否匹配
     * @param success
     * @param askEvents
     */
    private void checkAskEventParameter(boolean success, List<? extends AskEvent> askEvents) {

        List<Parameter> parameters = success ? successParameters : failureParameters;

        Set<Class<?>> methodParameterOfEventClass = parameters.stream()
                .map(Parameter::getType)
                .filter(BaseEvent.class::isAssignableFrom)
                .collect(Collectors.toSet());

        List<? extends Class<? extends AskEvent>> askEventClassList =
                askEvents.stream().map(x -> x.getClass()).collect(Collectors.toList());

        boolean allParameterValid = askEventClassList.stream()
                .allMatch(eventClass -> methodParameterOfEventClass.stream()
                        .anyMatch(parameterClass -> parameterClass.isAssignableFrom(eventClass)));

        if(!allParameterValid) {
            throw new EventException(String.format("回调类%s的%s方法参数不匹配, 方法声明: %s, 实际参数: %s",
                    callbackClassName, EventUtils.getAskCallbackMethodName(success),
                    methodParameterOfEventClass, askEventClassList));
        }
    }

    @Override
    public String toString() {
        return "AskEventCallback{" +
                "callbackClassName='" + callbackClassName + '\'' +
                ", callbackClass=" + callbackClass +
                '}';
    }
}
