package com.akkafun.common.event;

import com.akkafun.base.event.domain.AskEvent;

import java.util.List;
import java.util.Map;

/**
 * Created by liubin on 2016/6/6.
 */
public class AskParameter {

    private boolean united;

    private List<? extends AskEvent> askEvents;

    private Class<?> callbackClass;

    private Map<String, String> extraParams;

    protected AskParameter(boolean united, List<? extends AskEvent> askEvents,
                           Class<?> callbackClass, Map<String, String> extraParams) {
        this.united = united;
        this.askEvents = askEvents;
        this.callbackClass = callbackClass;
        this.extraParams = extraParams;
    }

    public boolean isUnited() {
        return united;
    }

    public List<? extends AskEvent> getAskEvents() {
        return askEvents;
    }

    public Class<?> getCallbackClass() {
        return callbackClass;
    }

    public Map<String, String> getExtraParams() {
        return extraParams;
    }
}
