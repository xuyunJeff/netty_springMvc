package com.eveb.saasops_msgsender.adapter;

import com.eveb.saasops_msgsender.config.NettyConstanct;
import com.eveb.saasops_msgsender.processor.MsgBean;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyChannelHandler extends SimpleChannelInboundHandler<MsgBean> {


    public MyChannelHandler() {
        super();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgBean msg) throws Exception {
        log.info("msg---buffer"+msg);
// 向客户端发送消息
        String response = new MsgBean("123321aaa","localhost","msg",NettyConstanct.REQUEST_PREFIX_MyChannel).toString();
        // 在当前场景下，发送的数据必须转换成ByteBuf数组
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        ByteBuf r =encoded.writeBytes(response.getBytes());
        ctx.writeAndFlush(r);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("MyChannelHandler-----客户端连接成功");
        // 向客户端发送消息
        /*String response = new MsgBean("123321aaa","localhost","msg",NettyConstanct.REQUEST_PREFIX_MyChannel).toString();
        // 在当前场景下，发送的数据必须转换成ByteBuf数组
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        ByteBuf r =encoded.writeBytes(response.getBytes());
        ctx.writeAndFlush(r);*/
//       ctx.writeAndFlush(new MsgBean("123321aaa","localhost","msg",NettyConstanct.REQUEST_PREFIX_MyChannel));
    }
   /* @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        log.info("MyChannelHandler      客户端连接断开");
    }*/
}
