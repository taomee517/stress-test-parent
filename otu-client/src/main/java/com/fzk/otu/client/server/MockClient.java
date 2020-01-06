package com.fzk.otu.client.server;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.handler.MockDeviceCodec;
import com.fzk.otu.client.handler.MockDeviceHandler;
import com.fzk.otu.client.util.ReconnectUtil;
import com.fzk.stress.util.ChannelSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class MockClient {
    private MockDevice device;
    private String ip;
    private int port;
    private AtomicInteger reconnectCounter = new AtomicInteger(0);

    private static int workers = Runtime.getRuntime().availableProcessors()*2;
    private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(workers);
    private static Bootstrap bootstrap = buildBootstrap();


    public MockClient(MockDevice device, String ip, int port) {
        if (!Boolean.TRUE.equals(device.isAgFinish())) {
            device.setTag206Info(StringUtils.join(ip,",",Integer.toHexString(port)));
            device.setTag20fInfo(StringUtils.join(ip,",",Integer.toHexString(port)));
        }
        device.setTag101Info(device.getImei());
        this.device = device;
        this.ip = ip;
        this.port = port;
    }


    public static Bootstrap buildBootstrap(){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,150,0, TimeUnit.SECONDS));
                        pipeline.addLast(new MockDeviceCodec());
                        pipeline.addLast(new MockDeviceHandler());
                    }
                });
        return bootstrap;
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(ip,port).addListeners(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        log.debug("设备imei={}与平台创建连接成功！",device.getImei());
                        Channel channel = future.channel();
                        if(Objects.nonNull(channel)){
                            ChannelSession.put(channel, ChannelSession.DEVICE,device);
                            ChannelSession.put(channel,ChannelSession.CLIENT,MockClient.this);
                        }
                    }else {
                        ReconnectUtil.buildReconnectTask(MockClient.this);
                    }
                }
            }).sync();
        } finally {
            return channelFuture;
        }

    }


    public void stop(){
        try {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
            bootstrap = null;
        } catch (Exception e) {
            log.error("关掉连接发生异常：{}", e);
        }
    }
}
