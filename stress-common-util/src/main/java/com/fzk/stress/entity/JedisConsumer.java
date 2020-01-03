package com.fzk.stress.entity;

import com.fzk.stress.cache.ChannelCache;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.ThreadPoolUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;

import static com.fzk.stress.cache.TopicCenter.CAR_STATUS_TOPIC;
import static com.fzk.stress.cache.TopicCenter.DELAY_MESSAGE_PREFIX;

@Slf4j
public class JedisConsumer extends JedisPubSub implements Runnable{

    @Override
    public void run() {
        RedisService.psubscribe(this, CAR_STATUS_TOPIC);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.debug("收到jedis监听消息,pattern:{}, topic:{}, msg:{}",pattern, channel, message);
        ThreadPoolUtil.pool.submit(new Runnable() {
            @Override
            public void run() {
                String imei = StringUtils.substringAfter(channel,":");
                log.debug("线程{}执行消息发布任务：imei:{}, time:{}",Thread.currentThread().getName(),imei,StringUtils.substringBetween(message,"331,",","));
                Channel ch = ChannelCache.IMEI_CHANNEL_CACHE.getIfPresent(imei);
                if(Objects.nonNull(ch) && ch.isActive()){
                    log.info("实时状态 ↑↑↑:{},imei: {}",message, imei );
                    ch.writeAndFlush(message);
                }else {
                    String key = StringUtils.join(DELAY_MESSAGE_PREFIX,imei);
                    RedisService.lpush(key,message);
                }
            }
        });

    }
}
