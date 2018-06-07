package com.eveb.saasops_msgsender.client;

import com.eveb.saasops_msgsender.config.NettyConstanct;
import com.eveb.saasops_msgsender.processor.MsgBean;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author William
 * @createTime 18-5-31:上午11:19
 * @Description: 客户端对应处理类(用一句话描述该文件做什么)
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<MsgBean> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("服务端连接成功");
        String response = new MsgBean("123321aaa","localhost","msg",NettyConstanct.REQUEST_PREFIX_MyChannel).toString();
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        ByteBuf r =encoded.writeBytes(response.getBytes());
        ctx.writeAndFlush(r);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             MsgBean msg) {
        log.info("Client received: " + msg.toString());    //3
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        log.info("断线");
        //cause.printStackTrace();
        ctx.close();
    }

}
