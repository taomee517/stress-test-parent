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
        List<String> imeis = FileInfoCheckUtil.getColumnData();

        /**单一设备验证时用*/
//        List<String> imeis = Arrays.asList("865886034429940");

        new Thread(new JedisConsumer()).start();
        HashedWheelTimer hashedWheelTimer = HashedWheelTimerUtil.instance().getTimer();
        int delaySign = LOGIN_COUNT_ONE_TICK;
        int size = imeis.size();
        for (int i=0; i<size; i++) {
            if(i%delaySign==0){
                Thread.sleep(HASH_WHEEL_TICK);
            }
            String imei = imeis.get(i);
            MockDevice device = new Ex223240Device();
            device.setAgFinish(false);
            device.setImei(imei);
            MockClient client = new MockClient(device, ACCEPTOR_IP, ACCEPTOR_PORT);
            TimerTask loginTask = new HashedWheelTask(client);
            hashedWheelTimer.newTimeout(loginTask,100, TimeUnit.MILLISECONDS);
        }
    }
}
