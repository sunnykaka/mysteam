package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.exception.EventException;
import com.akkafun.common.utils.JsonUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

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

    @SuppressWarnings("unchecked")
    public static EventType retrieveEventTypeFromJson(String payload) {
        //这里没有event的子类信息, 所以只能用map代替
        Map<String, Object> map = JsonUtils.json2Object(payload, Map.class);
        String type = (String)map.get("type");
        if(StringUtils.isBlank(type)) {
            throw new EventException(String.format("event type is blank, payload: %s", payload));
        }

        EventType eventType = EventType.valueOfIgnoreCase(type);
        if(eventType == null) {
            throw new EventException(String.format("unknown event type:%s, payload: %s", type, payload));
        }
        return eventType;

    }



}
