package com.akkafun.base.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by liubin on 2016/5/6.
 */
public class BooleanWrapper {

    private boolean result;

    @JsonCreator
    public BooleanWrapper(@JsonProperty("result") boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }
}
