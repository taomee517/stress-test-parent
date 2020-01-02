package com.fzk.stress.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import static com.fzk.stress.constants.Configuration.*;

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
        Jedis jedis = new Jedis(REDIS_IP,REDIS_PORT);
        jedis.select(REDIS_DATABASE);
        return jedis;
    }
}
