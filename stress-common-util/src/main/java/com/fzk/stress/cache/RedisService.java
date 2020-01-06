package com.fzk.stress.cache;

import com.fzk.stress.util.JedisBuilder;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.Set;

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
}
