package com.akkafun.common.event;

import com.akkafun.common.event.callbacks.AskTestEventFirstCallback;
import com.akkafun.common.event.callbacks.AskTestEventSecondCallback;
import com.akkafun.common.event.callbacks.UnitedTestEventCallback;
import com.akkafun.common.event.handlers.AskTestEventHandler;
import com.akkafun.common.event.handlers.NotifyFirstTestEventFirstHandler;
import com.akkafun.common.event.handlers.NotifyFirstTestEventSecondHandler;

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

    }

}
