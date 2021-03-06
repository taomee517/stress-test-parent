package com.fzk.otu.client;


import com.fzk.otu.client.client.MockClient;
import com.fzk.otu.client.consumer.JedisCarStatusConsumer;
import com.fzk.otu.client.consumer.JedisExpireEventConsumer;
import com.fzk.otu.client.device.Ex223240Device;
import com.fzk.otu.client.device.MockDevice;
import com.fzk.otu.client.task.ClientConnectTask;
import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.stress.util.HashedWheelTimerUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fzk.stress.constants.Configuration.*;

@Slf4j
public class PressureTest {
    private static int ipIndex = -1;

    public static void main(String[] args) throws Exception {
        List<String> imeis = FileInfoCheckUtil.getColumnData();

        /**单一设备验证时用*/
//        List<String> imeis = Arrays.asList("865886034429940");

        new Thread(new JedisCarStatusConsumer()).start();
        new Thread(new JedisExpireEventConsumer()).start();
        HashedWheelTimer hashedWheelTimer = HashedWheelTimerUtil.instance().getTimer();
        int delaySign = LOGIN_COUNT_ONE_TICK;
        int size = imeis.size();
        String ip = null;
        for (int i=0; i<size; i++) {
            if(i%delaySign==0){
                Thread.sleep(HASH_WHEEL_TICK);
            }
            String imei = imeis.get(i);
            MockDevice device = new Ex223240Device();
            device.setAgFinish(false);
            device.setImei(imei);
            if(i%LOGIN_COUNT_ONE_IP==0){
                ipIndex++;
                if(ipIndex >= LOCAL_ADDRS.size()){
                    log.error("压测设备量太大，请增加ip");
                    break;
                }else {
                    ip = LOCAL_ADDRS.get(ipIndex);
                    log.info("切换ip = {}", ip);
                }
            }
            InetSocketAddress local = new InetSocketAddress(ip,0);
            MockClient client = new MockClient(device, ACCEPTOR_IP, ACCEPTOR_PORT, local);
            TimerTask loginTask = new ClientConnectTask(client);
            hashedWheelTimer.newTimeout(loginTask,100, TimeUnit.MILLISECONDS);
        }
    }
}
