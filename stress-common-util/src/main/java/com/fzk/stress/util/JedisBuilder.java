package com.fzk.stress.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class JedisBuilder {

    private static class SingletonHolder {
        public final static JedisBuilder instance = new JedisBuilder();
    }

    public static JedisBuilder instance() {
        return JedisBuilder.SingletonHolder.instance;
    }

    private JedisBuilder(){

    }

    public Jedis getJedis(){
        Jedis jedis = new Jedis("127.0.0.1",6379);
        jedis.select(2);
        return jedis;
    }
}
