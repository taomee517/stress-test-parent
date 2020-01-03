package com.fzk.otu.client.util;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.server.MockClient;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.util.ThreadPoolUtil;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientConnectUtil {
    private static final  List<Integer> CONNECTION_INTERVAL_LIST = new ArrayList<>(Arrays.asList(60,120,180,240,320,400,480,560,640,720,800,880,960,1040,1120,1200));
    private static final int ON_CONNECTION_INTERVAL = 5;
    private static volatile Map<String,Thread> LAST_SCHEDULE_THREAD_MAP = new HashMap<>(8);

    public static ChannelFuture scheduledNextConnectionTask(MockClient client) throws Exception{
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
//        Thread lastThread = LAST_SCHEDULE_THREAD_MAP.get(imei);
//        if(Objects.nonNull(lastThread)){
//            log.info("关闭上一定时线程：imei = {}, thread = {}",imei, Thread.currentThread().getName());
//            lastThread.interrupt();
//        }
//        log.info("{}秒后重连！imei = {}", nextConnectDelay, imei);
//        ScheduledFuture<Channel> channelScheduledFuture = ThreadPoolUtil.schedule.schedule(new Callable<Channel>() {
//            @Override
//            public Channel call() throws Exception {
//                log.info("保存定时线程：imei = {}, thread = {}",imei, Thread.currentThread().getName());
//                LAST_SCHEDULE_THREAD_MAP.put(imei,Thread.currentThread());
//                return client.connect();
//            }
//        },nextConnectDelay, TimeUnit.SECONDS);

        log.info("{}秒后重连！imei = {}", nextConnectDelay, imei);
        String reconnectExpireKey = TopicCenter.buildReconnectExpireKey(imei);
        RedisService.setEx(reconnectExpireKey,nextConnectDelay);
        CountDownLatch latch = new CountDownLatch(1);
        JedisExpireCall jedisExpireCall = new JedisExpireCall(client,latch);
        Future<ChannelFuture> channelTaskResult = ThreadPoolUtil.pool.submit(new Callable<ChannelFuture>() {
            @Override
            public ChannelFuture call() throws Exception {
                new Thread(jedisExpireCall).start();
                latch.await();
                return jedisExpireCall.getChannelFuture();
            }
        });
        return channelTaskResult.get();
    }
}
