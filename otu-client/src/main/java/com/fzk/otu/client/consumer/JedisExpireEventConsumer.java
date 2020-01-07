package com.fzk.otu.client.consumer;

import com.fzk.stress.cache.ChannelCache;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;

import static com.fzk.stress.constants.Configuration.RESEND_MSG_INTERVAL;

/**
 * @author taomee517@qq.com
 */
@Slf4j
@Data
public class JedisExpireEventConsumer extends JedisPubSub implements Runnable {

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
            String imeiAndTime = StringUtils.substringAfter(msg,TopicCenter.RESEND_TOPIC);
            String imei = StringUtils.substringBefore(imeiAndTime, "-");
            String timestamp = StringUtils.substringAfter(imeiAndTime, "-");
            log.info("状态消息未收到ACK,重发: expireKey = {}",msg);
            Channel channel = ChannelCache.IMEI_CHANNEL_CACHE.getIfPresent(imei);
            String resendCopyKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_COPY_TOPIC,imei);
            String resendMsg = RedisService.queryAndDeleteMsgByTime(resendCopyKey,timestamp);
            if(StringUtils.isBlank(resendMsg)){
                log.error("没有查找到重发消息，resendMsg = {}",resendMsg);
                return;
            }
            if (Objects.nonNull(channel) && channel.isActive()) {
                channel.writeAndFlush(resendMsg);
            }else {
                RedisService.setEx(msg,RESEND_MSG_INTERVAL);
            }
        }
    }
}
