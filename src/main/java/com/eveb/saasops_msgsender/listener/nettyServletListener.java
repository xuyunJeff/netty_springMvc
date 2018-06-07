package com.eveb.saasops_msgsender.listener;

import com.eveb.saasops_msgsender.adapter.*;
import com.eveb.saasops_msgsender.config.AppConfig;
import com.eveb.saasops_msgsender.config.NettyConstanct;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author William
 * @createTime 18-5-29:下午11:46
 * @Description: netty 服务器配置(用一句话描述该文件做什么)
 */
@Component
public class nettyServletListener {


   private  DispatcherServlet dispatcherServlet;

   @Bean("Msg_DispatcherServlet")
   public DispatcherServlet initDispatcherServlet() {
        return dispatcherServlet;
    }

    public nettyServletListener() throws ServletException {
        MockServletContext servletContext = new MockServletContext();
        MockServletConfig servletConfig = new MockServletConfig(servletContext);
        AnnotationConfigWebApplicationContext wac = new AnnotationConfigWebApplicationContext();
        wac.setServletContext(servletContext);
        wac.setServletConfig(servletConfig);
        wac.register(AppConfig.class);
        wac.refresh();
        this.dispatcherServlet = new DispatcherServlet(wac);
        this.dispatcherServlet.init(servletConfig);
    }
    @Autowired
    public void  initNettyServlet() throws Exception{

        ServletConfig servletConfig =dispatcherServlet.getServletConfig();
        //1用于服务端接受客户端的连接
        EventLoopGroup acceptorGroup = new NioEventLoopGroup();
        //2用于进行SocketChannel的网络读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //Netty用于启动NIO服务器的辅助启动类
            ServerBootstrap sb = new ServerBootstrap();
            //将两个NIO线程组传入辅助启动类中
            sb.group(acceptorGroup, workerGroup)
                    //设置创建的Channel为NioServerSocketChannel类型
                    .channel(NioServerSocketChannel.class)
                    //配置NioServerSocketChannel的TCP参数
                    .option(ChannelOption.SO_BACKLOG, NettyConstanct.MAX_THREADS)
                    //设置绑定IO事件的处理类
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                        @Override
                        protected void initChannel(SocketChannel arg0) throws Exception {
                            ChannelPipeline pipeline = arg0.pipeline();

                            pipeline.addLast(new MyChannelDecoder());
                            pipeline.addLast(new MyChannelHandler());
                            //支持Http协议
                            //Http请求处理的编解码器
                            //用于将HTTP请求进行封装为FullHttpRequest对象
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(NettyConstanct.WEB_SOCKET_READ_LENGTH));

                            //Http请求的具体处理对象
                            pipeline.addLast(new WebSocketServerProtocolHandler(NettyConstanct.REQUEST_PREFIX_SOCKET));
                            pipeline.addLast(new WebSocketHandler());
                            pipeline.addLast(new HttpHandler(dispatcherServlet));
                            pipeline.addLast(new MyChannelEncoder());

                       /*   pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());*/
                            //支持WebSocket协议

                            //处理文件流
                            //pipeline.addLast(new ChunkedWriteHandler());
                        }
                    });
            //绑定端口，同步等待成功（sync()：同步阻塞方法，等待bind操作完成才继续）
            //ChannelFuture主要用于异步操作的通知回调
            ChannelFuture cf = sb.bind(NettyConstanct.port).sync();
            System.out.println("服务端启动在"+NettyConstanct.port+"端口。");
            //等待服务端监听端口关闭
            cf.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放线程池资源
            acceptorGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new nettyServletListener().initNettyServlet();;
    }
}
