

package com.eveb.saasops_msgsender.client;

import com.eveb.saasops_msgsender.adapter.MyChannelDecoder;
import com.eveb.saasops_msgsender.adapter.MyChannelEncoder;
import com.eveb.saasops_msgsender.adapter.WebSocketHandler;
import com.eveb.saasops_msgsender.config.NettyConstanct;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author William
 * @createTime 18-5-31:上午11:19
 * @Description: 客户端(用一句话描述该文件做什么)
 */

public class Client {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        int port=7878; //服务端默认端口
        new Client().connect(port, "localhost");
    }

    public void connect(int port, String host) throws Exception{
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                        protected void initChannel(SocketChannel pipeline) throws Exception {
                            //pipeline.pipeline().addLast(new WebSocketHandler());
                            //pipeline.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            /*pipeline.pipeline().addLast("decoder", new StringDecoder());
                            pipeline.pipeline().addLast("encoder", new StringEncoder());*/
                            pipeline.pipeline().addLast(new MyChannelDecoder());
                            pipeline.pipeline().addLast(new MyChannelEncoder());
                            pipeline.pipeline().addLast(new ClientHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture cf = bs.connect(host, port).sync();
            //等待客户端链路关闭
            cf.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }
}
