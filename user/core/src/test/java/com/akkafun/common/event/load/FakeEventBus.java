package com.akkafun.common.event.load;

/**
 * Created by liubin on 2016/5/30.
 */
public class FakeEventBus {

    public void askUnited(FakeUnitedAskEventCallback callback, FakeAskEvent firstAskEvent, FakeAskEvent secondAskEvent,
                          FakeAskEvent... remainAskEvents) {
        System.out.println(callback.getClass());
    }

}
