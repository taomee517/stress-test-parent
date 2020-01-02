package com.fzk.stress.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

public class ChannelCache {
    public static final long EXPIRE_TIME = 10*60*1000L;
    public static final Cache<String, Channel> IMEI_CHANNEL_CACHE;

    static {
        IMEI_CHANNEL_CACHE = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_TIME, TimeUnit.MILLISECONDS).build();
    }
}
