package com.akkafun.common.event.load;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Created by liubin on 2016/5/30.
 */
public class FakeAskCouponUse extends FakeAskEvent {

    private String id;

    public FakeAskCouponUse() {
        this.id = RandomStringUtils.randomAlphanumeric(8);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FakeAskCouponUse{" +
                "id='" + id + '\'' +
                "} " + super.toString();
    }
}
