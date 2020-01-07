package com.fzk.otu.client.handler;


import com.fzk.otu.client.device.MockDevice;
import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.util.ChannelSession;
import com.fzk.stress.util.ConvertUtil;
import com.fzk.stress.util.OTUCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import static com.fzk.stress.constants.Configuration.RESEND_MSG_INTERVAL;

@Slf4j
public class MockDeviceCodec extends ByteToMessageCodec<String> {
    private static final String NEED_ACK_STATUS_SIGN = "|7|331,";
    private static final String STATUS_ACK_SIGN = "|8|331";

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        //如果是7|331消息，必须收到平台的回复
        //如果没有收到回复，需要重新上报
        if(StringUtils.countMatches(msg,NEED_ACK_STATUS_SIGN)>0){
            String imei = ((MockDevice) ChannelSession.get(ctx.channel(), ChannelSession.DEVICE)).getImei();
            String resendKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_TOPIC,imei);
            //加上定位消息的时间戳，否则redis中存储的值会被覆盖
            String timestamp = StringUtils.substringBetween(msg,NEED_ACK_STATUS_SIGN,",");
            String resendKeyWithTime = StringUtils.joinWith("-", resendKey, timestamp);
            String resendCopyKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_COPY_TOPIC,imei);
            RedisService.setEx(resendKeyWithTime,RESEND_MSG_INTERVAL);
            RedisService.lpush(resendCopyKey, msg);
        }
        byte[] bytes = msg.getBytes();
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String msg = OTUCodecUtil.Byte2StringSerialize(in);
        try {
            if (StringUtils.isNotEmpty(msg)) {
                String func = StringUtils.substring(msg,4,7);
                if(StringUtils.countMatches(func,"|")==0){
                    msg = ConvertUtil.decrypt(msg);
                }
            }
            if (Objects.nonNull(msg)) {
                out.add(msg);

                //处理ack监听
                if(StringUtils.countMatches(msg,STATUS_ACK_SIGN)>0) {
                    String imei = ((MockDevice) ChannelSession.get(ctx.channel(), ChannelSession.DEVICE)).getImei();
                    String resendKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_TOPIC,imei);
                    String pattern = StringUtils.join(resendKey,"*");
                    String minTtlKey = RedisService.getAndDeleteMinTtlKey(pattern,RESEND_MSG_INTERVAL);
                    if (StringUtils.isNotBlank(minTtlKey)) {
                        String timestamp = StringUtils.substringAfterLast(minTtlKey, "-");
                        log.info("收到状态回复并删除最小过期时间 key = {}", minTtlKey);
                        String resendCopyKey = TopicCenter.buildCommonKey(TopicCenter.RESEND_COPY_TOPIC,imei);
                        RedisService.queryAndDeleteMsgByTime(resendCopyKey,timestamp);
                    }
                }
            }
        }catch (Exception e){
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            log.error("报文解析发生异常：src = {},msg = {},e ={}",new String(bytes),msg,e);
            throw  e;
        }finally {
            resetBuffer(ctx,in);
            //验证一下discardSomeReadBytes和resetBuffer的效果是否相同 TODO
//            in.discardSomeReadBytes();
        }
    }


    /**
     * 移动指针到开始位置
     *
     * @param ctx
     * @param in
     */
    private void resetBuffer(ChannelHandlerContext ctx, ByteBuf in) {
        int left = in.readableBytes();
        int start = in.readerIndex();
        if (left > 0 && in.readerIndex() > 0) {
            for (int index = 0; index < left; index++) {
                in.setByte(index, in.getByte(index + start));
            }
            in.setIndex(0, left);
        }
    }
}
