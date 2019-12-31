package com.fzk.otu.client.handler;


import com.fzk.stress.util.ConvertUtil;
import com.fzk.stress.util.OTUCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
public class MockDeviceCodec extends ByteToMessageCodec<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
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
