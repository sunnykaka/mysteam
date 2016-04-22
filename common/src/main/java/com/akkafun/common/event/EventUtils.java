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

    /**
     * 将事件转成json
     * @param event
     * @return
     */
    public static String serializeEvent(BaseEvent event) {
        return JsonUtils.object2Json(event);
    }

    /**
     * 将json转成事件对象
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends BaseEvent> T deserializeEvent(String data, Class<T> clazz) {
        return JsonUtils.json2Object(data, clazz);
    }

    /**
     * 从事件json中得到事件id和类型
     * @param payload
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object[] retrieveIdAndEventTypeFromJson(String payload) {
        Map<String, Object> map = retrieveEventMapFromJson(payload);
        String type = (String)map.get("type");

        String id = (String)map.get("id");
        EventType eventType = EventType.valueOfIgnoreCase(type);

        return new Object[]{id, eventType};
    }


    /**
     *
     * @param payload
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> retrieveEventMapFromJson(String payload) {
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


        String id = (String)map.get("id");
        if(StringUtils.isBlank(id)) {
            throw new EventException(String.format("event id is blank, payload: %s", payload));
        }

        return map;

    }





}
