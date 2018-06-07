package com.eveb.saasops_msgsender.adapter;

import com.eveb.saasops_msgsender.processor.MsgBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;
import org.msgpack.MessagePack;

public class MyChannelEncoder extends MessageToByteEncoder<MsgBean> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MsgBean msg, ByteBuf out) throws Exception {
        // TODO Auto-generated method stub
        out.writeBytes(new MessagePack().write(msg));
    }


}