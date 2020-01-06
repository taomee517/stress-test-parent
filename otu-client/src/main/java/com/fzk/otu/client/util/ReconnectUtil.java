package com.fzk.otu.client.util;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.server.MockClient;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.HashedWheelTimerUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ReconnectUtil {
    private static final  List<Integer> CONNECTION_INTERVAL_LIST = new ArrayList<>(Arrays.asList(60,120,180,240,320,400,480,560,640,720,800,880,960,1040,1120,1200));
    private static final int ON_CONNECTION_INTERVAL = 5;

    public static int getNextConnectInterval(MockClient client){
        MockDevice device = client.getDevice();
        if(Objects.isNull(device)){
            log.error("设备信息为空！");
        }
        int nextConnectDelay = 0;
        String imei = device.getImei();
        boolean on = RedisService.getOnStatus(imei);
        if(on){
            client.setReconnectCounter(new AtomicInteger(0));
            nextConnectDelay = ON_CONNECTION_INTERVAL;
        }else {
            int delayIndex = client.getReconnectCounter().getAndAdd(1);
            int lastIndexOfIntervalList = CONNECTION_INTERVAL_LIST.size()-1;
            if (delayIndex >= lastIndexOfIntervalList) {
                nextConnectDelay = CONNECTION_INTERVAL_LIST.get(lastIndexOfIntervalList);
            }else {
                nextConnectDelay = CONNECTION_INTERVAL_LIST.get(delayIndex);
            }
        }
        return nextConnectDelay;
    }


    public static void buildReconnectTask(MockClient client){
        HashedWheelTask task = new HashedWheelTask(client);
        int nextConnectInterval = getNextConnectInterval(client);
        log.info("设备imei = {}，{}秒后与平台发起重连",client.getDevice().getImei(),nextConnectInterval);
        HashedWheelTimerUtil.instance().getTimer().newTimeout(task,nextConnectInterval, TimeUnit.SECONDS);
    }
}
