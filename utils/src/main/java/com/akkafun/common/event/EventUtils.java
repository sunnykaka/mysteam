package com.akkafun.common.event;

import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.utils.JsonUtils;

/**
 * Created by liubin on 2016/4/11.
 */
public class EventUtils {

    public static String serializeEvent(BaseEvent event) {
        return JsonUtils.object2Json(event);
    }

    public static <T extends BaseEvent> T deserializeEvent(String data, Class<T> clazz) {
        return JsonUtils.json2Object(data, clazz);
    }


}
