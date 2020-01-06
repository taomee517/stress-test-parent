package com.fzk.stress.constants;

public class Configuration {


    /**压测文件地址*/
    public static final String DEVICE_LIST_PATH = "E:\\private\\test\\pressure test\\DEMO.xlsx";

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

    /**redis配置*/
    public static final String REDIS_IP = "127.0.0.1";
    public static final int REDIS_PORT = 6379;
    public static final int REDIS_DATABASE = 2;


    /**重新建连的时间间隔（秒）*/
    public static final int RECONNECT_INTERVAL = 60;

    /**on状态时状态上报间隔时间（秒）*/
    public static final int ON_REPORT_INTERVAL = 5;
    public static final int NONE_ON_REPORT_INTERVAL = 10;

    /**定位消息重发间隔时间（秒）*/
    public static final int RESEND_MSG_INTERVAL = 30;
    /**须重发的定位消息缓存备份过期时间（秒）,
     * 可以比间隔时间长一点
     * 足够相关业务逻辑处理完成即可
     */
    public static final int RESEND_MSG_COPY_TTL = 40;

}
