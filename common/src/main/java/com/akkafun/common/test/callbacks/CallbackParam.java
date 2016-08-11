package com.akkafun.common.test.callbacks;

import com.akkafun.base.event.constants.FailureInfo;
import com.akkafun.base.event.domain.AskEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liubin on 2016/6/17.
 */
public class CallbackParam {

    private List<AskEvent> askEvents;

    private FailureInfo failureInfo;

    private Map<String, String> params;

    public CallbackParam(List<AskEvent> askEvents, FailureInfo failureInfo, Map<String, String> params) {
        this.askEvents = askEvents;
        this.failureInfo = failureInfo;
        this.params = params == null ? new HashMap<>() : params;
    }


    public List<AskEvent> getAskEvents() {
        return askEvents;
    }

    public FailureInfo getFailureInfo() {
        return failureInfo;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
