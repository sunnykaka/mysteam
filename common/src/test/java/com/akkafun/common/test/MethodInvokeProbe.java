package com.akkafun.common.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by liubin on 2016/6/16.
 */
public class MethodInvokeProbe {

    private ConcurrentMap<String, Integer> invokeCountMap = new ConcurrentHashMap<>();

    public void record(String methodName) {
        invokeCountMap.compute(methodName, (key, count) -> count == null ? 1 : count + 1);
    }

    public boolean isInvokeSpecifyTimes(String methodName, int times) {
        return times == invokeCountMap.getOrDefault(methodName, 0);
    }

}
