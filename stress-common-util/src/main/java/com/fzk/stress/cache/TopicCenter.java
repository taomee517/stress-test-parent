package com.fzk.stress.cache;

import org.apache.commons.lang3.StringUtils;

public class TopicCenter {
    public static final String CAR_STATUS_PREFIX = "CarStatusQueue:";
    public static final String CAR_STATUS_TOPIC = "CarStatusQueue:*";

    public static final String DELAY_MESSAGE_PREFIX = "DelayMessageQueue:";

    public static final String ON_STATUS_PREFIX = "ON:";


    public static String buildOnKey(String imei){
        return StringUtils.join(ON_STATUS_PREFIX,imei);
    }
}
