package com.fzk.otu.client;


import com.fzk.otu.client.entity.Ex223240Device;
import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.server.MockClient;
import com.fzk.otu.client.util.HashedWheelTask;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.entity.JedisConsumer;
import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.stress.util.HashedWheelTimerUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fzk.stress.constants.Configuration.*;

@Slf4j
public class PressureTest {

    public static void main(String[] args) throws Exception {
        RedisService.clearAllOnStatus();
        RedisService.clearAllDelayMessage();
        List<String> imeis = FileInfoCheckUtil.getColumnData();
//        List<String> imeis = Arrays.asList("865886034429940");
        new Thread(new JedisConsumer()).start();
        HashedWheelTimer hashedWheelTimer = HashedWheelTimerUtil.instance().getTimer();
        int delaySign = LOGIN_COUNT_ONE_SECOND;
        int size = imeis.size();
        for (int i=0; i<size; i++) {
            String imei = imeis.get(i);
            int delayUnit = i/delaySign + 1;
            MockDevice device = new Ex223240Device();
            device.setAgFinish(false);
            device.setImei(imei);
            MockClient client = new MockClient(device, ACCEPTOR_IP, ACCEPTOR_PORT);
            TimerTask loginTask = new HashedWheelTask(client);
            hashedWheelTimer.newTimeout(loginTask,1000 * delayUnit, TimeUnit.MILLISECONDS);
        }
    }
}
