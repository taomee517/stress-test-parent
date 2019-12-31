package com.fzk.otu.client;


import com.fzk.otu.client.entity.Ex223240Device;
import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.entity.RequestType;
import com.fzk.otu.client.server.MockClient;
import com.fzk.otu.client.util.MessageBuilder;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class PressureTest {
//    private static String ip = "pre.acceptor.mysirui.com";
//    private static String ip = "192.168.6.200";
//    private static String ip = "127.0.0.1";
//    private static String ip = "192.168.7.71";
//    private static String ip = "192.168.2.61";
    private static String ip = "192.168.4.111";
    private static int port = 2103;

    public static void main(String[] args) throws Exception {
        //标明记录压测IMEI号的xls地址
//        String filePath = "E:\\private\\test\\pressure test\\yxd - half.xlsx";
//        String filePath = "E:\\private\\test\\pressure test\\压测设备.xlsx";
//        List<String> imeis = FileInfoCheckUtil.getColumnData(filePath);

        List<String> imeis = Arrays.asList("863613035276886","865258037560719","863613035280920");
        int delaySign = 16;
        int size = imeis.size();
        for (int i=0; i<size; i++) {
            Thread.sleep(500);
            String imei = imeis.get(i);
            MockDevice device = new Ex223240Device();
            device.setAgFinish(false);
            device.setImei(imei);
            MockClient client = new MockClient(device, ip, port);
            Channel channel = client.connect();
            String msg = MessageBuilder.buildAgAsMsg(RequestType.AS,device);
            log.info("登录 ↑↑↑：{}，imei = {}",msg,device.getImei());
            channel.writeAndFlush(msg);
        }
    }
}
