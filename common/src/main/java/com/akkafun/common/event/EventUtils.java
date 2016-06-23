package com.akkafun.common.event;

import com.akkafun.base.event.constants.EventType;
import com.akkafun.base.event.constants.FailureReason;
import com.akkafun.base.event.domain.BaseEvent;
import com.akkafun.common.utils.JsonUtils;
import com.akkafun.common.event.constant.AskEventStatus;
import com.akkafun.common.exception.EventException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by liubin on 2016/4/11.
 */
public class EventUtils {

    public static final String SUCCESS_CALLBACK_NAME = "onSuccess";

    public static final String FAILED_CALLBACK_NAME = "onFailure";

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
     *
     * @param payload
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> retrieveEventMapFromJson(String payload) {
        //这里没有event的子类信息, 所以只能用map代替
        Map<String, Object> map = JsonUtils.json2Object(payload, Map.class);
        String type = (String)map.get("type");
        if(StringUtils.isBlank(type)) {
            throw new EventException(String.format("event type is blank, payload: %s", payload));
        }
        if(EventType.valueOfIgnoreCase(type) == null) {
            throw new EventException(String.format("unknown event type:%s, payload: %s", type, payload));
        }
        if(map.get("id") == null) {
            throw new EventException(String.format("event id is blank, payload: %s", payload));
        }

        return map;

    }


    public static String getAskCallbackMethodName(boolean success) {
        return success ? SUCCESS_CALLBACK_NAME : FAILED_CALLBACK_NAME;
    }

    public static FailureReason fromAskEventStatus(AskEventStatus status) {

        switch (status) {
            case CANCELLED:
                return FailureReason.CANCELLED;
            case FAILED:
                return FailureReason.FAILED;
            case TIMEOUT:
                return FailureReason.TIMEOUT;
            default:
                throw new EventException("unknown FailureReason from AskEventStatus: " + status);
        }

    }

    public static AskEventStatus fromFailureReason(FailureReason reason) {

        switch (reason) {
            case CANCELLED:
                return AskEventStatus.CANCELLED;
            case FAILED:
                return AskEventStatus.FAILED;
            case TIMEOUT:
                return AskEventStatus.TIMEOUT;
            default:
                throw new EventException("unknown AskEventStatus from FailureReason: " + reason);
        }

    }


}
