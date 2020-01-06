package com.fzk.stress.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ChannelCache {
    public static final long EXPIRE_TIME = 10*60*1000L;
    public static final Cache<String, Channel> IMEI_CHANNEL_CACHE;

    static {
        IMEI_CHANNEL_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_TIME, TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<String, Channel>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Channel> removalNotification) {
                        log.warn("设备与平台的通道缓存被移除，imei:{},channel：{},cause:{} "
                                ,removalNotification.getKey()
                                ,removalNotification.getValue()
                                ,removalNotification.getCause());
                    }
                }).build();
    }
}
