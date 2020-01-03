package com.fzk.otu.client.server;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.handler.MockDeviceCodec;
import com.fzk.otu.client.handler.MockDeviceHandler;
import com.fzk.otu.client.util.ClientConnectUtil;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class MockClient {
    private MockDevice device;
    private String ip;
    private int port;
    private AtomicInteger reconnectCounter = new AtomicInteger(0);

    private static int workers = Runtime.getRuntime().availableProcessors()*8;
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
                        pipeline.addLast(new IdleStateHandler(0,1000*150,0, TimeUnit.MILLISECONDS));
                        pipeline.addLast(new MockDeviceCodec());
                        pipeline.addLast(new MockDeviceHandler());
                    }
                });
        return bootstrap;
    }

    public Channel connect() {
        Channel channel = null;
        try {
            ChannelFuture channelFuture = bootstrap.connect(ip,port).sync();
            channel = channelFuture.channel();
        } catch (Exception e) {
            log.info("连接失败，重连,imei= {}",device.getImei());
            ScheduledFuture<Channel> channelScheduledFuture = ClientConnectUtil.scheduledNextConnectionTask(this);
            try {
                channel = channelScheduledFuture.get();
            }catch (InterruptedException inex){

            } catch (Exception ex) {
                log.error("重连发生异常：{}",ex);
            }
        }finally {
            if(Objects.nonNull(channel)){
                ChannelSession.put(channel, ChannelSession.DEVICE,device);
                ChannelSession.put(channel,ChannelSession.CLIENT,this);
            }
            return channel;
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
