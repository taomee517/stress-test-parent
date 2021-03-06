package com.fzk.stress.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {


    /**压测文件地址*/
    public static final String DEVICE_LIST_PATH = "E:\\private\\test\\pressure test\\DEMO.xlsx";

    /**本地ip列表*/
    public static final List<String> LOCAL_ADDRS = new ArrayList<>(Arrays.asList("127.0.0.1", "192.168.179.1"));

    /**Acceptor配置*/
    public static final String ACCEPTOR_IP = "127.0.0.1";
    public static final int ACCEPTOR_PORT = 2103;

    /**
     * 时间轮的时间间隔(毫秒)
     * 为保证工具的反应效率，这个值不要高于1秒
     */
    public static final int HASH_WHEEL_TICK = 100;
    /**每个时间间隔上线的设备数*/
    public static final int LOGIN_COUNT_ONE_TICK = 8;
    /**一个IP最大设备数*/
    public static final int LOGIN_COUNT_ONE_IP = 25000;

    /**redis配置*/
    public static final String REDIS_IP = "127.0.0.1";
    public static final int REDIS_PORT = 6379;
    public static final int REDIS_DATABASE = 2;

    /**on状态时状态上报间隔时间（秒）*/
    public static final int ON_REPORT_INTERVAL = 5;
    public static final int NONE_ON_REPORT_INTERVAL = 150;

    /**定位消息重发间隔时间（秒）*/
    public static final int RESEND_MSG_INTERVAL = 30;

}
