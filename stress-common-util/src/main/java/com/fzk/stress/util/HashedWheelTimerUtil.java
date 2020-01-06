package com.fzk.stress.util;

import io.netty.util.HashedWheelTimer;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

import static com.fzk.stress.constants.Configuration.HASH_WHEEL_TICK;

@Getter
public class HashedWheelTimerUtil {

    private HashedWheelTimer timer = new HashedWheelTimer(HASH_WHEEL_TICK, TimeUnit.MILLISECONDS);
    private static class SingletonHolder {
        public final static HashedWheelTimerUtil instance = new HashedWheelTimerUtil();
    }

    public static HashedWheelTimerUtil instance() {
        return SingletonHolder.instance;
    }

    private HashedWheelTimerUtil(){

    }

}
