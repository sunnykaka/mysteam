package com.akkafun.common.test.callbacks;

import com.akkafun.common.test.domain.AskTestEvent;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by liubin on 2016/6/16.
 */
public class AskTestEventFirstCallback {

    public static final List<CallbackParam> successParams = new CopyOnWriteArrayList<>();

    public void onSuccess(AskTestEvent event) {
        CallbackParam callbackParam = new CallbackParam(Lists.newArrayList(event), null, null);
        successParams.add(callbackParam);
    }

}
