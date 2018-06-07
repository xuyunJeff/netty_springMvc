package com.eveb.saasops_msgsender.adapter;


import com.alibaba.fastjson.JSON;
import com.eveb.saasops_msgsender.config.NettyConstanct;
import com.eveb.saasops_msgsender.processor.ServerChannelHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 
 * @author william
 * @createTime 2018年5月30日
 * 
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     * @param context
     * @param webSocketFrame
     */
    private void handWebSocketFrame(ChannelHandlerContext context,WebSocketFrame webSocketFrame) {
        if (webSocketFrame instanceof CloseWebSocketFrame) {//判断是否是关闭websocket的指令
            handshaker.close(context.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
        }
        if (webSocketFrame instanceof PingWebSocketFrame) {//判断是否是ping消息
            context.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        if (!(webSocketFrame instanceof TextWebSocketFrame)) {//判断是否是二进制消息
            System.out.println("不支持二进制消息");
            throw new RuntimeException(this.getClass().getName());
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        String request = ((TextWebSocketFrame) webSocketFrame).text();
        System.out.println("服务端收到客户端的消息：" + request);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(context.channel().id() + ":" + request);
        //服务端向每个连接上来的客户端发送消息
        //把客户端信息保存起来和唯一的用户信息
        //[login] {'SToken':'adasdeewre','token':'123231'}
        Channel currentChannel = context.channel();
        if(request.startsWith("[login]")){
            Map<String,String> loginMap =(Map)JSON.parse(request.replace("[login]",""));
            ServerChannelHandlerAdapter.addChannelMap(loginMap.get("SToken"),loginMap.get("token"),currentChannel.id());
            context.writeAndFlush(new TextWebSocketFrame("浏览器连接成功"));
        }
        //[send] {'SToken':'adasdeewre','token':'123231','msg':'欢迎来到银河系'}
        if(request.startsWith("[send]")){
            //返回发送者
            Map<String,String> msgMap =(Map)JSON.parse(request.replace("[send]",""));
            context.writeAndFlush(new TextWebSocketFrame(msgMap.get("msg")+"  发送成功"));
            ServerChannelHandlerAdapter.group.forEach(channel ->{
                if(channel.id().equals(ServerChannelHandlerAdapter.getTargetChannel(msgMap.get("SToken"),msgMap.get("token")))){
                    channel.writeAndFlush(new TextWebSocketFrame(msgMap.get("msg")));
                }
            });
        }
    }


    /**
     * 处理客户端向服务端发起http握手请求业务
     * @param context
     * @param fullHttpRequest
     */
    public static void handHttpRequest(ChannelHandlerContext context,FullHttpRequest fullHttpRequest){
        String uri =fullHttpRequest.getUri();
        if (!fullHttpRequest.getDecoderResult().isSuccess() ||!("websocket".equals(fullHttpRequest.headers().get("Upgrade")))){//判断是否http握手请求
            sendHttpResponse(context,fullHttpRequest, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory(NettyConstanct.WEB_SOCKET_URL,null,false);
        handshaker = webSocketServerHandshakerFactory.newHandshaker(fullHttpRequest);
        if (handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(context.channel());
        }else{
            handshaker.handshake(context.channel(),fullHttpRequest);

        }
    }

    /**
     * 服务端想客户端发送响应消息
     * @param context
     * @param fullHttpRequest
     * @param defaultFullHttpResponse
     */
    private static void sendHttpResponse(ChannelHandlerContext context, FullHttpRequest fullHttpRequest, DefaultFullHttpResponse defaultFullHttpResponse){
        if (defaultFullHttpResponse.getStatus().code() != 200){
            ByteBuf buf = Unpooled.copiedBuffer(defaultFullHttpResponse.getStatus().toString(), CharsetUtil.UTF_8);
            defaultFullHttpResponse.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture future = context.channel().writeAndFlush(defaultFullHttpResponse);
        if (defaultFullHttpResponse.getStatus().code() !=200){
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    //客户端与服务端创建链接的时候调用
    @Override
    public void channelActive (ChannelHandlerContext context)throws Exception{
        ServerChannelHandlerAdapter.group.add(context.channel());
        log.info("WebSocket -----客户端与服务端连接开启");
        context.channel().writeAndFlush(new TextWebSocketFrame("hello"));
    }
    //客户端与服务端断开连接的时候调用
    /*@Override
    public void channelInactive(ChannelHandlerContext context)throws Exception{
        log.info(context.channel().id().asShortText());
        ServerChannelHandlerAdapter.group.remove(context.channel());
        log.info("WebSocket    客户端与服务端连接断开");
    }*/
    //服务端接收客户端发送过来的数据结束之后调用
    @Override
    public void channelReadComplete(ChannelHandlerContext context)throws Exception{
        context.flush();
    }
    //工程出现异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable)throws Exception{
        throwable.printStackTrace();
        context.close();
    }

}
