package com.akkafun.common.event.load;

/**
 * Created by liubin on 2016/5/30.
 */
public interface FakeUnitedAskEventCallback {

    void onSuccess(FakeAskEvent[] askEvents);

    void onFailure(FakeAskEvent[] askEvents);

}
