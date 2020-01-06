package com.fzk.otu.client.util;

import com.fzk.otu.client.server.MockClient;
import com.fzk.stress.cache.ChannelCache;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.util.ChannelSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Data
public class JedisExpireConsumer extends JedisPubSub implements Runnable {
    private MockClient client;
    private ChannelFuture channelFuture;

    public JedisExpireConsumer(MockClient client) {
        this.client = client;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.debug("过期事件，patter:{},channel:{}，message:{}",pattern,channel,message);
        resendNoneAckMsg(message);
    }

    @Override
    public void run(){
        RedisService.psubscribe(this, TopicCenter.REDIS_EXPIRED_TOPIC);
    }

    private void resendNoneAckMsg(String message){
        String msg = message.toString();
        if (msg.startsWith(TopicCenter.RESEND_TOPIC)) {
            String targetImei = client.getDevice().getImei();
            String expireImei = StringUtils.substringAfter(msg,TopicCenter.RESEND_TOPIC);
            if (StringUtils.equalsIgnoreCase(expireImei,targetImei)) {
                log.info("状态消息重发，imei = {}",targetImei);
                Channel channel = ChannelCache.IMEI_CHANNEL_CACHE.getIfPresent(targetImei);
                String resendCopyKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_COPY_TOPIC,targetImei);
                String resendMsg = RedisService.pop(resendCopyKey);
                if (channel.isActive() && StringUtils.isNotBlank(resendMsg)) {
                    channel.writeAndFlush(resendMsg);
                }
            }
        }
    }
}
