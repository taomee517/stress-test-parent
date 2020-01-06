package com.fzk.otu.client.handler;

import com.fzk.otu.client.entity.MockDevice;
import com.fzk.otu.client.entity.RequestType;
import com.fzk.otu.client.server.MockClient;
import com.fzk.otu.client.util.MessageBuilder;
import com.fzk.otu.client.util.ReconnectUtil;
import com.fzk.stress.cache.ChannelCache;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.ChannelSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.fzk.stress.cache.TopicCenter.DELAY_MESSAGE_PREFIX;

@Data
@Slf4j
public class MockDeviceHandler extends ChannelInboundHandlerAdapter {
    /**最终维护的channel*/
    private Channel channel;
//    /**登录成功标志*/
//    private boolean loginSuccess;
    /**控制相关的标签*/
    private static List<String> controlTag = new ArrayList<>(Arrays.asList("511","512","513","514","515","516","517","518","519","51a","51b","51c","51d","51e","51f"));


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        log.info("channel active，channel = {}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MockClient client = (MockClient) ChannelSession.get(ctx.channel(),ChannelSession.CLIENT);
        MockDevice device = (MockDevice) ChannelSession.get(ctx.channel(),ChannelSession.DEVICE);
        if (Objects.equals(ctx.channel(),this.channel)) {
            log.error("与业务平台断连,imei = {}, channel = {}",device.getImei(), ctx.channel());
        }else {
            log.error("与寻址平台断连,imei = {}, channel = {}",device.getImei(), ctx.channel());
        }
        ChannelCache.IMEI_CHANNEL_CACHE.invalidate(device.getImei());
        //如果正在重连就不用再添加重连任务了
        Boolean isReconnect = ((Boolean) ChannelSession.get(channel, ChannelSession.RECONNECT));
        if(Objects.nonNull(isReconnect) && isReconnect){
            return;
        }
        if (!(Objects.nonNull(this.channel) && this.channel.isActive())) {
            ChannelSession.put(channel,ChannelSession.RECONNECT,Boolean.TRUE);
            try {
                ReconnectUtil.buildReconnectTask(client);
            } catch (Exception ex) {
                log.error("重连发生异常：{}",ex);
            }
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String serverMsg = msg.toString();
        MockDevice device = (MockDevice) ChannelSession.get(ctx.channel(),ChannelSession.DEVICE);
        log.info("SERVER ↓↓↓: {}, imei: {}", serverMsg, device.getImei());
        //更新缓存
        updateImeiChannel(device.getImei(),ctx.channel());
        String resp = null;
        if(StringUtils.isNotBlank(serverMsg)){
            if (StringUtils.countMatches(serverMsg,"|a2|621")>0) {
                String ip = StringUtils.substringBetween(serverMsg, "|621,", ",");
                String portTemp = StringUtils.substringAfter(serverMsg,ip + ",");
                int port = 0;
                if (StringUtils.isNotBlank(portTemp)) {
                    String portHex = tailTrim(portTemp);
                    port = Integer.valueOf(portHex,16);
                }
                device.setAgFinish(true);
                String ipNport = device.getTag206Info();
                String[] ipPortArray = StringUtils.split(ipNport,",");
                String remoteIp = ipPortArray[0];
                int remotePort = Integer.valueOf(ipPortArray[1],16);

                //收到登录服务器地址，如果地址一样，则直接登录，不一样须重新建连
                Channel channel = null;
                if (StringUtils.equals(ip,remoteIp) && remotePort == port) {
                    channel = ctx.channel();
                    ChannelSession.put(channel,ChannelSession.DEVICE,device);
                }else{
                    MockClient client = new MockClient(device,ip,port);
                    channel = client.connect().channel();
                }
                if (Objects.nonNull(channel)) {
                    this.channel = channel;
                    String asMsg = MessageBuilder.buildAgAsMsg(RequestType.AS,device);
                    log.info("登录 ↑↑↑：{}，imei = {}",msg,device.getImei());
                    channel.writeAndFlush(asMsg);
                }
            }else if(StringUtils.countMatches(serverMsg,"|a4|")>0){
                log.info("设备：{}登录成功！", device.getImei());
                //登录成功后将channel缓存起来
                ChannelSession.put(channel, ChannelSession.LAST_CACHE_TIME, System.currentTimeMillis());
                ChannelCache.IMEI_CHANNEL_CACHE.put(device.getImei(), channel);
                //向平台上报一些基本配置
                MessageBuilder.publishBaseInfo(device,ctx);
                //监听定位消息
                listenVehicleStatus(device.getImei(), ctx.channel());
            }else {
                String[] units = StringUtils.split(serverMsg,"|");
                if(units.length<3){
                    return;
                }
                String function = units[1];
                String content = units[2];
                String[] tagNparas = StringUtils.split(content,",");
                String tag = tagNparas[0];
                String value = null;
                value = buildMultiParaValue(tagNparas,false);
                if(!MessageBuilder.isSupportedTag(device,tag)){
                    //不支持的标签
                    resp = MessageBuilder.buildNotSupportResp(tag);
                }else if(StringUtils.equals(RequestType.QUERY.getFunction(),function)){
                    //查询
                    resp = MessageBuilder.buildMessage(RequestType.QUERY_ACK,device,tag);
                }else if(StringUtils.equals(RequestType.SETTING.getFunction(),function)){
                    //设置
                    resp = StringUtils.replace(serverMsg,"|3|","|4|");
                    if(StringUtils.countMatches(serverMsg,"1*")==0){
                        resp = StringUtils.replace(resp,"*","1*");
                    }
                    resp = MessageBuilder.outQuote(resp);
                    MessageBuilder.deviceInfoSetting(device,tag,value);
                }else if(StringUtils.equals(RequestType.WRITE.getFunction(),function)){
                    //透传设置
                    if(StringUtils.equals("613", tag)){
                        String[] bParas = StringUtils.split(value,",");
                        if(bParas.length<2){
                            log.error("613透传消息格式不对，msg = {}", serverMsg);
                            return;
                        }
                        String attachId = bParas[0];
                        String bFuncNtag = bParas[1];
                        String[] bFuncNtagArr = StringUtils.split(bFuncNtag,"#");
                        String bFunc = bFuncNtagArr[0];
                        String bTag = bFuncNtagArr[1];
                        String bValue = buildMultiParaValue(bParas,true);
                        if(StringUtils.equals(RequestType.SETTING.getFunction(),bFunc)){
                            resp = StringUtils.replace(serverMsg,"5|613","5|614");
                            resp = StringUtils.replace(resp,"3#b","4#b");
                            if(StringUtils.countMatches(serverMsg,"1*")==0){
                                resp = StringUtils.replace(resp,"*","1*");
                            }
                            resp = MessageBuilder.outQuote(resp);
                        }
                    }else if(StringUtils.equals("6a3", tag)){
                        String ipNport = device.getTag206Info();
                        String[] ipPortArray = StringUtils.split(ipNport,",");
                        String ip = ipPortArray[0];
                        int port = Integer.valueOf(ipPortArray[1],16);
                        log.info("设备重启！");
                        MockClient client = new MockClient(device,ip,port);
                        client.connect();
                    }
                }else if(StringUtils.equals(RequestType.PUBLISH.getFunction(),function)){
                    resp = MessageBuilder.buildMessage(RequestType.PUBLISH_ACK,device,tag);
                    if(controlTag.contains(tag)){
                        String resultTag = StringUtils.replace(tag,"51","41");

                        //先变更车辆控制结果
                        String[] cmdValueArray = StringUtils.split(value, ",");
                        String serial = cmdValueArray[0];
                        String executeResult = "1";
                        String respValue = StringUtils.joinWith(",",executeResult,serial);
                        MessageBuilder.deviceInfoSetting(device,resultTag,respValue);
//                        log.info("更新后：411 = {}",device.getTag411Info());

                        //再组装回复内容并回复平台
                        String controlResult = MessageBuilder.buildMessage(RequestType.PUBLISH,device,resultTag);
                        log.info("控车 ↑↑↑：{}, imei: {}", controlResult, device.getImei());
                        ctx.writeAndFlush(controlResult);
                    }
                }else if(StringUtils.equals(RequestType.PUBLISH_ACK.getFunction(),function)){
                    //平台的ACK，可以不做任何处理
                }else {
                    log.error("暂时不支持的消息类型：msg = {}",serverMsg);
                }
            }

            if(StringUtils.isNotBlank(resp)){
                log.info("回复 ↑↑↑：{}, imei: {}", resp, device.getImei());
                ctx.writeAndFlush(resp);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        MockDevice device = (MockDevice) ChannelSession.get(ctx.channel(),ChannelSession.DEVICE);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            if(event.state().equals(IdleState.WRITER_IDLE)){
                String heatbeat = "()";
                log.info("心跳 ↑↑↑：{}, imei: {}", heatbeat, device.getImei());
                ctx.channel().writeAndFlush(heatbeat);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MockDeviceHandler处理发生异常：{}", cause);
        ctx.close();
    }



    private String tailTrim(String tempData) {
        if (tempData.endsWith(",")) {
            tempData = tempData.substring(0, tempData.length() - 1);
        }
        if (tempData.endsWith(",|")) {
            tempData = tempData.substring(0, tempData.length() - 2);
        }
        if (tempData.endsWith("|")) {
            tempData = tempData.substring(0, tempData.length() - 1);
        }
        if (tempData.endsWith(",|)")) {
            tempData = tempData.substring(0, tempData.length() - 3);
        }
        if (tempData.endsWith("|)")) {
            tempData = tempData.substring(0, tempData.length() - 2);
        }

        return tempData;
    }

    private String buildMultiParaValue(String[] tagNparas,boolean isTransmit){
        if(tagNparas.length<2 && !isTransmit){
            return "";
        }else if(tagNparas.length<3 && isTransmit){
            return "";
        }else {
            int i = 1;
            if(isTransmit){
                i = 2;
            }
            StringBuilder sb = new StringBuilder();
            for(;i<tagNparas.length;i++){
                String para = tagNparas[i];
                if(i==tagNparas.length-1){
                    para = tailTrim(para);
                }
                sb.append(para);
                sb.append(",");
            }
            return StringUtils.removeEnd(sb.toString(),",");
        }
    }

    //如果上次更新缓存距当前超过了缓存过期时间的一半，就再次更新
    private void updateImeiChannel(String imei,Channel channel){
        Channel lastChannel = ChannelCache.IMEI_CHANNEL_CACHE.getIfPresent(imei);
        if(Objects.nonNull(lastChannel)){
            long lastUpdateTime = ((long) ChannelSession.get(lastChannel, ChannelSession.LAST_CACHE_TIME));
            if((System.currentTimeMillis()-lastUpdateTime) >= ChannelCache.EXPIRE_TIME/2){
                ChannelSession.put(channel,ChannelSession.LAST_CACHE_TIME,System.currentTimeMillis());
                ChannelCache.IMEI_CHANNEL_CACHE.put(imei,channel);
            }
        }
    }


    private void listenVehicleStatus(String imei,Channel channel){
        String key = StringUtils.join(DELAY_MESSAGE_PREFIX, imei);
        String msg = null;
        while ((msg = RedisService.pop(key)) != null){
            log.info("积压状态 ↑↑↑:{},imei: {}",msg, imei );
            channel.writeAndFlush(msg);
        }
    }
}
