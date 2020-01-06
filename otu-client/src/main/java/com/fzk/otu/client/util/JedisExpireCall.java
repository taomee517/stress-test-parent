package com.fzk.otu.client.util;

import com.fzk.otu.client.server.MockClient;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Data
public class JedisExpireCall extends JedisPubSub implements Runnable {
    private MockClient client;
    private ChannelFuture channelFuture;
    private CountDownLatch latch;

    public JedisExpireCall(MockClient client,CountDownLatch latch) {
        this.client = client;
        this.latch = latch;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.debug("过期事件，patter:{},channel:{}，message:{}",pattern,channel,message);
        acquireChannel(message);
    }

    @Override
    public void run(){
        RedisService.psubscribe(this, TopicCenter.RECONNECT_EXPIRED_TOPIC);
    }

    private void acquireChannel(String message){
        String msg = message.toString();
        if (msg.startsWith(TopicCenter.RECONNECT_TOPIC)) {
            String targetImei = client.getDevice().getImei();
            String expireImei = StringUtils.substringAfter(msg,TopicCenter.RECONNECT_TOPIC);
            if (StringUtils.equalsIgnoreCase(expireImei,targetImei)) {
                log.info("设备重连，释放latch, imei = {}", targetImei);
                try {
                    this.channelFuture = client.connect();
                } finally {
                    latch.countDown();
                }
            }
        }
    }
}
