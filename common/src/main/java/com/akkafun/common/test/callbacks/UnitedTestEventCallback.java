package com.akkafun.common.test.callbacks;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.common.test.domain.AskTestEvent;
import com.akkafun.common.test.domain.RevokableAskTestEvent;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/16.
 */
public class UnitedTestEventCallback {

    public static final String SUCCESS_EVENT_NAME = "克尔苏加德";

    public static final List<CallbackParam> successParams = new CopyOnWriteArrayList<>();
    public static final List<CallbackParam> failureParams = new CopyOnWriteArrayList<>();


    public void onSuccess(AskTestEvent askTestEvent, RevokableAskTestEvent revokableAskTestEvent,
                          String param1, String param2) {
        Map<String, String> params = new HashMap<>();
        params.put("param1", param1);
        params.put("param2", param2);
        CallbackParam callbackParam = new CallbackParam(Lists.newArrayList(askTestEvent, revokableAskTestEvent),
                null, params);
        successParams.add(callbackParam);
    }

    public void onFailure(AskTestEvent askTestEvent, RevokableAskTestEvent revokableAskTestEvent,
                          FailureInfo failureInfo, String param3, String param4) {
        Map<String, String> params = new HashMap<>();
        params.put("param3", param3);
        params.put("param4", param4);
        CallbackParam callbackParam = new CallbackParam(Lists.newArrayList(askTestEvent, revokableAskTestEvent),
                failureInfo, params);
        failureParams.add(callbackParam);
    }

}
