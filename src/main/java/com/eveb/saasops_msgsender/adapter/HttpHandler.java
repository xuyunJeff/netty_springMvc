package com.eveb.saasops_msgsender.adapter;

import com.eveb.saasops_msgsender.config.NettyConstanct;
import com.eveb.saasops_msgsender.processor.ServerChannelHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author william
 * @createTime 2018年3月25日 下午8:14:41
 * 
 */
@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static String uriPrefix = NettyConstanct.REQUEST_PREFIX_HTTP;
    private static   Servlet servlet;

    public HttpHandler(Servlet servlet) throws ServletException {
        super();
        this.servlet =servlet;
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		log.info("HttpHandler    "+msg.toString());
        String uri =msg.getUri();
        handShake(ctx,msg,uri);
	}

	public static void handShake(ChannelHandlerContext ctx, FullHttpRequest msg,String uri )throws Exception{
        if(uri.startsWith(uriPrefix)) {
            //处理http请求
            if(msg instanceof HttpRequest){
                boolean flag = HttpMethod.POST.equals(msg.getMethod())
                        || HttpMethod.GET.equals(msg.getMethod());

                Map<String, String>  parammap = getRequestParams(ctx,msg);
                if(flag && ctx.channel().isActive()){
                    //HTTP请求、GET/POST
                    MockHttpServletResponse servletResponse = new MockHttpServletResponse();
                    MockHttpServletRequest servletRequest =new MockHttpServletRequest(servlet.getServletConfig().getServletContext());
                    // headers
                    for (String name : msg.headers().names()) {
                        for (String value : msg.headers().getAll(name)) {
                            servletRequest.addHeader(name, value);
                        }
                    }
                    uri = new String(uri.getBytes("ISO8859-1"), "UTF-8");
                    uri = URLDecoder.decode(uri, "UTF-8");
                    UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri).build();
                    String path = uriComponents.getPath();
                    path = URLDecoder.decode(path, "UTF-8");
                    servletRequest.setRequestURI(path);
                    servletRequest.setServletPath(path);
                    servletRequest.setMethod(msg.getMethod().name());

                    if (uriComponents.getScheme() != null) {
                        servletRequest.setScheme(uriComponents.getScheme());
                    }
                    if (uriComponents.getHost() != null) {
                        servletRequest.setServerName(uriComponents.getHost());
                    }
                    if (uriComponents.getPort() != -1) {
                        servletRequest.setServerPort(uriComponents.getPort());
                    }

                    ByteBuf content = msg.content();
                    content.readerIndex(0);
                    byte[] data = new byte[content.readableBytes()];
                    content.readBytes(data);
                    servletRequest.setContent(data);
                    if (uriComponents.getQuery() != null) {
                        String query = UriUtils.decode(uriComponents.getQuery(),"UTF-8");
                        servletRequest.setQueryString(query);
                    }
                    if(parammap!=null&&parammap.size()>0){
                        for (String key : parammap.keySet()) {
                            servletRequest.addParameter(UriUtils.decode(key,"UTF-8"), UriUtils.decode(parammap.get(key) == null ? "": parammap.get(key), "UTF-8"));
                        }
                    }
                    servlet.service(servletRequest,servletResponse);
                    HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse.getStatus());
                    String result = servletResponse.getContentAsString();
                    result = StringUtils.isEmpty(result)?"":result;
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
                    response.headers().set("Content-Type", "text/json;charset=UTF-8");
                    response.headers().set("Access-Control-Allow-Origin", "*");
                    response.headers().set("Access-Control-Allow-Headers", "Content-Type,Content-Length, Authorization, Accept,X-Requested-With,X-File-Name");
                    response.headers().set("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
                    response.headers().set("Content-Length", Integer.valueOf(response.content().readableBytes()));
                    response.headers().set("Connection", "keep-alive");
                    ChannelFuture writeFuture = ctx.writeAndFlush(response);
                    writeFuture.addListener(ChannelFutureListener.CLOSE);
                }

            }
        }else if(uri.startsWith(NettyConstanct.REQUEST_PREFIX_SOCKET)){
            WebSocketHandler.handHttpRequest(ctx,msg);
        }
    }

    /**
     * 获取post请求、get请求的参数保存到map中
     */
    private static Map<String, String> getRequestParams(ChannelHandlerContext ctx, HttpRequest req){
        Map<String, String>requestParams=new HashMap<String, String>();
        // 处理get请求
        if (req.getMethod() == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());
            Map<String, List<String>> parame = decoder.parameters();
            Iterator<Map.Entry<String, List<String>>> iterator = parame.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, List<String>> next = iterator.next();
                requestParams.put(next.getKey(), next.getValue().get(0));
            }
        }
        // 处理POST请求
        if (req.getMethod() == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                    new DefaultHttpDataFactory(false), req);
            List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
            for(InterfaceHttpData data:postData){
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    MemoryAttribute attribute = (MemoryAttribute) data;
                    requestParams.put(attribute.getName(), attribute.getValue());
                }
            }
        }
        return requestParams;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("http      客户端连接成功");
        super.channelActive(ctx);
    }

  /*  @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        log.info("http      客户端连接断开");
    }*/
}
