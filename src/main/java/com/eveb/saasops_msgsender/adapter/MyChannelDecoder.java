package com.eveb.saasops_msgsender.adapter;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops_msgsender.config.NettyConstanct;
import com.eveb.saasops_msgsender.processor.MsgBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.msgpack.MessagePack;

import java.util.List;

public class MyChannelDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            // 实现反序列化
            int length = in.readableBytes();
            byte[] array = new byte[length];
            String content = new String(array, in.readerIndex(), length);
            String str = new String(in.toString(CharsetUtil.UTF_8));

            if(content != null && !"".equals(content.trim())){
                //判断是否是我的自定义协议
                if(!NettyConstanct.isMyChannel(content)){
                    ctx.channel().pipeline().remove(this);
                    return;
                }
            }else if(str != null && !"".equals(str.trim())){
                if(!str.contains(NettyConstanct.REQUEST_PREFIX_MyChannel)){
                    ctx.channel().pipeline().remove(this);
                    return;
                }
            }
            in.getBytes(in.readerIndex(), array, 0, length);
            MsgBean msgBean=JSON.parseObject(str,MsgBean.class);
            out.add(msgBean);
            in.clear();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Auto-generated catch block
            ctx.channel().pipeline().remove(this);
        }
    }

}