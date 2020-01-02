package com.fzk.otu.client;


import com.fzk.otu.client.entity.Ex223240Device;
import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.entity.RequestType;
import com.fzk.otu.client.server.MockClient;
import com.fzk.otu.client.util.MessageBuilder;
import com.fzk.stress.entity.JedisConsumer;
import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.stress.util.ThreadPoolUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.fzk.stress.constants.Configuration.*;

@Slf4j
public class PressureTest {

    public static void main(String[] args) throws Exception {
        List<String> imeis = FileInfoCheckUtil.getColumnData();
        ThreadPoolUtil.pool.submit(new JedisConsumer());
        int delaySign = 16;
        int size = imeis.size();
        for (int i=0; i<size; i++) {
            Thread.sleep(500);
            String imei = imeis.get(i);
            MockDevice device = new Ex223240Device();
            device.setAgFinish(false);
            device.setImei(imei);
            MockClient client = new MockClient(device, ACCEPTOR_IP, ACCEPTOR_PORT);
            Channel channel = client.connect();
            String msg = MessageBuilder.buildAgAsMsg(RequestType.AS,device);
            log.info("登录 ↑↑↑：{}，imei = {}",msg,device.getImei());
            channel.writeAndFlush(msg);
        }
    }
}
