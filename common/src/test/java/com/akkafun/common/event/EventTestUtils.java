package com.akkafun.common.event;

import com.akkafun.common.test.callbacks.AskTestEventFirstCallback;
import com.akkafun.common.test.callbacks.AskTestEventSecondCallback;
import com.akkafun.common.test.callbacks.UnitedTestEventCallback;
import com.akkafun.common.test.handlers.AskTestEventHandler;
import com.akkafun.common.test.handlers.NotifyFirstTestEventFirstHandler;
import com.akkafun.common.test.handlers.NotifyFirstTestEventSecondHandler;
import com.akkafun.common.test.handlers.RevokableAskTestEventHandler;

/**
 * Created by liubin on 2016/6/17.
 */
public class EventTestUtils {

    public static void clear() {
        AskTestEventFirstCallback.successParams.clear();
        AskTestEventSecondCallback.successParams.clear();
        AskTestEventSecondCallback.failureParams.clear();
        UnitedTestEventCallback.successParams.clear();
        UnitedTestEventCallback.failureParams.clear();
        AskTestEventHandler.events.clear();
        NotifyFirstTestEventFirstHandler.events.clear();
        NotifyFirstTestEventSecondHandler.events.clear();
        RevokableAskTestEventHandler.events.clear();
        RevokableAskTestEventHandler.revokeEvents.clear();

    }

}
