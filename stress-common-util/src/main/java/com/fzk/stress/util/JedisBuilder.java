package com.fzk.stress.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static com.fzk.stress.constants.Configuration.*;

@Slf4j
public class JedisBuilder {
    private static JedisPool jedisPool;

    private static class SingletonHolder {
        public final static JedisBuilder instance = new JedisBuilder();
    }

    public static JedisBuilder instance() {
        return JedisBuilder.SingletonHolder.instance;
    }

    private JedisBuilder(){

    }

//    static {
//        JedisPoolConfig config = new JedisPoolConfig();
//        //是否启用后进先出, 默认true
//        config.setLifo(true);
//        //最大连接数, 默认8个
//        config.setMaxTotal(8);
//        //最大空闲连接数, 默认8个
//        config.setMaxIdle(8);
//        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),
//        // 如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
//        config.setMaxWaitMillis(20000);
//        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
//        config.setBlockWhenExhausted(true);
//        jedisPool = new JedisPool(config, REDIS_IP, REDIS_PORT);
//    }

    public Jedis getJedis(){
//        Jedis jedis = jedisPool.getResource();
        Jedis jedis = new Jedis(REDIS_IP,REDIS_PORT);
        jedis.select(REDIS_DATABASE);
        return jedis;
    }
}
