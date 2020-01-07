package com.fzk.otu.client.task;

import com.fzk.otu.client.device.MockDevice;
import com.fzk.otu.client.util.RequestType;
import com.fzk.otu.client.client.MockClient;
import com.fzk.otu.client.util.MessageBuilder;
import io.netty.channel.ChannelFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ClientConnectTask implements TimerTask {
    private MockClient client;

    public ClientConnectTask(MockClient client) {
        this.client = client;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        MockDevice device = client.getDevice();
        log.debug("设备imei={}向平台发起登录！", device.getImei());
        ChannelFuture channelFuture = client.connect();
        if (Objects.nonNull(channelFuture)
                && channelFuture.isSuccess()
                && Objects.nonNull(channelFuture.channel())
                && channelFuture.channel().isActive()) {
            String msg = MessageBuilder.buildAgAsMsg(RequestType.AS,device);
            log.info("登录 ↑↑↑：{}，imei = {}",msg,device.getImei());
            channelFuture.channel().writeAndFlush(msg);
        }
    }
}
