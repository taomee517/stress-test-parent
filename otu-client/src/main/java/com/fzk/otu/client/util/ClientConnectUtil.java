package com.fzk.otu.client.util;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.server.MockClient;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.ThreadPoolUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientConnectUtil {
    private static final  List<Integer> CONNECTION_INTERVAL_LIST = new ArrayList<>(Arrays.asList(60,120,180,240,320,400,480,560,640,720,800,880,960,1040,1120,1200));
    private static final int ON_CONNECTION_INTERVAL = 5;
    private static volatile Map<String,Thread> LAST_SCHEDULE_THREAD_MAP = new HashMap<>(8);

    public static ScheduledFuture<Channel> scheduledNextConnectionTask(MockClient client){
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
        Thread lastThread = LAST_SCHEDULE_THREAD_MAP.get(imei);
        if(Objects.nonNull(lastThread)){
            log.debug("关闭上一定时线程：{}",lastThread.getName());
            lastThread.interrupt();
        }
        log.info("{}秒后重连！imei = {}", nextConnectDelay, imei);
        ScheduledFuture<Channel> channelScheduledFuture = ThreadPoolUtil.schedule.schedule(new Callable<Channel>() {
            @Override
            public Channel call() throws Exception {
                log.debug("保存定时线程：{}",Thread.currentThread().getName());
                LAST_SCHEDULE_THREAD_MAP.put(imei,Thread.currentThread());
                return client.connect();
            }
        },nextConnectDelay, TimeUnit.SECONDS);

        return channelScheduledFuture;
    }
}
