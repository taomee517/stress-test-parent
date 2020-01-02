package com.fzk.stress.constants;

public class Configuration {


    /**压测文件地址*/
    public static final String DEVICE_LIST_PATH = "E:\\private\\test\\pressure test\\DEMO.xlsx";

    /**Acceptor配置*/
    public static final String ACCEPTOR_IP = "127.0.0.1";
    public static final int ACCEPTOR_PORT = 2103;

    /**重新建连的时间间隔（秒）*/
    public static final int RECONNECT_INTERVAL = 60;

    /**redis配置*/
    public static final String REDIS_IP = "127.0.0.1";
    public static final int REDIS_PORT = 6379;
    public static final int REDIS_DATABASE = 2;

}
