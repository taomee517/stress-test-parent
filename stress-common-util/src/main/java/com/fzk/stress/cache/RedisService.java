package com.fzk.stress.cache;

import com.fzk.stress.util.JedisBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;

@Slf4j
public class RedisService {

    public static void lpush(String key, String value){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
           jedis.lpush(key,value);
        } finally {
            jedis.close();
        }
    }


    public static long publish(String topic, String value){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            return jedis.publish(topic,value);
        } finally {
            jedis.close();
        }
    }


    public static void psubscribe(JedisPubSub jedisPubSub, String topic){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            jedis.psubscribe(jedisPubSub,topic);
        } finally {
            jedis.close();
        }
    }

    public static String pop(String key){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            return jedis.rpop(key);
        } finally {
            jedis.close();
        }
    }

    public static void lrem(String key,String value){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            jedis.lrem(key,0, value);
        } finally {
            jedis.close();
        }
    }


    public static String get(String key){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            return jedis.get(key);
        } finally {
            jedis.close();
        }
    }

    public static void setEx(String key, int timeOut){
        setEx(key,"", timeOut);
    }

    public static void setEx(String key, String value, int timeOut){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            jedis.setex(key, timeOut, value);
        } finally {
            jedis.close();
        }
    }

    public static void set(String key,boolean flag){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            jedis.set(key,Boolean.toString(flag));
        } finally {
            jedis.close();
        }
    }

    public static void delete(String key){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            jedis.del(key);
        } finally {
            jedis.close();
        }
    }

    public static long llen(String key){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            return jedis.llen(key);
        } finally {
            jedis.close();
        }
    }

    public static String getAndDeleteMinTtlKey(String parttern,long expireTime){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            Set<String> keys = jedis.keys(parttern);
            long size = keys.size();
            String minTtlKey = null;
            if (Objects.nonNull(keys) && size!=0) {
                long tempTtl = expireTime + 1;
                Iterator<String> iterator = keys.iterator();
                for(int i=0;i<size;i++){
                    String key = iterator.next();
                    long ttl = jedis.ttl(key);
                    log.info("key=" + key + ",ttl= " + ttl);
                    if(ttl<tempTtl && ttl>0){
                        tempTtl = ttl;
                        minTtlKey = key;
                    }
                }
                log.info("delete minTtlKey = " + minTtlKey);
                delete(minTtlKey);
            }
            return minTtlKey;
        } finally {
            jedis.close();
        }
    }

    public static String queryAndDeleteMsgByTime(String key,String timestamp){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            List<String> msgs = jedis.lrange(key,0,-1);
            if (Objects.nonNull(msgs) && msgs.size()!=0) {
                for (String msg:msgs) {
                    if(StringUtils.countMatches(msg,timestamp)>0){
                        jedis.lrem(key,-1,msg);
                        return msg;
                    }
                }
            }
            return null;
        } finally {
            jedis.close();
        }
    }


    public static long getDelayMessageSize(String imei){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            String key = TopicCenter.buildDelayMessageKey(imei);
            return llen(key);
        } finally {
            jedis.close();
        }
    }

    public static  boolean getOnStatus(String imei){
        String onKey = TopicCenter.buildOnKey(imei);
        String srcOn = get(onKey);
        boolean on = StringUtils.isNotBlank(srcOn) && StringUtils.equalsIgnoreCase("true", srcOn);
        return on;
    }

    public static void clearAllOnStatus(){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            Set<String> onKeys =  jedis.keys(StringUtils.join(TopicCenter.ON_STATUS_PREFIX, "*"));
            if(Objects.nonNull(onKeys) && onKeys.size()!=0){
                for(String key:onKeys){
                    jedis.del(key);
                }
            }
        } finally {
            jedis.close();
        }
    }

    public static void clearAllDelayMessage(){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            Set<String> onKeys =  jedis.keys(StringUtils.join(TopicCenter.DELAY_MESSAGE_PREFIX, "*"));
            if(Objects.nonNull(onKeys) && onKeys.size()!=0){
                for(String key:onKeys){
                    jedis.del(key);
                }
            }
        } finally {
            jedis.close();
        }
    }


    public static void main(String[] args) {
        String minKey =  getAndDeleteMinTtlKey("a:123-*",1000);
        System.out.println(minKey);
    }
}
