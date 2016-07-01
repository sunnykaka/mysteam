package com.akkafun.common.event;

import com.akkafun.base.event.domain.AskEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by liubin on 2016/6/6.
 */
public class AskParameter {

    private boolean united;

    private List<? extends AskEvent> askEvents;

    private Class<?> callbackClass;

    private Map<String, String> extraParams;

    private Optional<LocalDateTime> timeoutTime;

    protected AskParameter(boolean united, List<? extends AskEvent> askEvents,
                           Class<?> callbackClass, Map<String, String> extraParams, Optional<LocalDateTime> timeoutTime) {
        this.united = united;
        this.askEvents = askEvents;
        this.callbackClass = callbackClass;
        this.extraParams = extraParams;
        this.timeoutTime = timeoutTime;
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

    public Optional<LocalDateTime> getTimeoutTime() {
        return timeoutTime;
    }
}
