package com.fzk.stress.entity;

import com.fzk.stress.util.JedisBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

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
}
