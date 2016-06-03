package com.akkafun.common.event.load;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Created by liubin on 2016/5/30.
 */
public class FakeAskDeductBalance extends FakeAskEvent {

    private String id;

    public FakeAskDeductBalance() {
        this.id = RandomStringUtils.randomAlphanumeric(8);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FakeAskDeductBalance{" +
                "id='" + id + '\'' +
                "} " + super.toString();
    }
}
