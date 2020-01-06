package com.fzk.stress.cache;

import org.apache.commons.lang3.StringUtils;

public class TopicCenter {
    public static final String CAR_STATUS_PREFIX = "CarStatusQueue:";
    public static final String CAR_STATUS_TOPIC = "CarStatusQueue:*";

    public static final String DELAY_MESSAGE_PREFIX = "DelayMessageQueue:";

    public static final String ON_STATUS_PREFIX = "ON:";

    public static final String REDIS_EXPIRED_TOPIC = "__keyevent@2__:expired";
    public static final String RECONNECT_TOPIC = "Reconnect:";

    public static final String RESEND_TOPIC = "Resend:";

    public static final String RESEND_COPY_TOPIC = "ResendCopy:";


    public static String buildOnKey(String imei){
        return buildCommonKey(ON_STATUS_PREFIX,imei);
    }

    public static String buildReconnectExpireKey(String imei){
        return buildCommonKey(RECONNECT_TOPIC,imei);
    }

    public static String buildDelayMessageKey(String imei){
        return buildCommonKey(DELAY_MESSAGE_PREFIX,imei);
    }

    public static String buildCommonKey(String type,String imei){
        return StringUtils.join(type,imei);
    }
}
