package com.fzk.otu.client;


import com.fzk.otu.client.entity.Ex223240Device;
import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.entity.RequestType;
import com.fzk.otu.client.server.MockClient;
import com.fzk.otu.client.util.MessageBuilder;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.entity.JedisConsumer;
import com.fzk.stress.util.FileInfoCheckUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static com.fzk.stress.constants.Configuration.ACCEPTOR_IP;
import static com.fzk.stress.constants.Configuration.ACCEPTOR_PORT;

@Slf4j
public class PressureTest {

    public static void main(String[] args) throws Exception {
        RedisService.clearAllOnStatus();
        List<String> imeis = FileInfoCheckUtil.getColumnData();
        new Thread(new JedisConsumer()).start();
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
            if (Objects.nonNull(channel)) {
                String msg = MessageBuilder.buildAgAsMsg(RequestType.AS,device);
                log.info("登录 ↑↑↑：{}，imei = {}",msg,device.getImei());
                channel.writeAndFlush(msg);
            }
        }
    }
}
