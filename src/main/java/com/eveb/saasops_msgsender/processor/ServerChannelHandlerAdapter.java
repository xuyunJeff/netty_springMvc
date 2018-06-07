package com.eveb.saasops_msgsender.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 放在内存，也可放在缓存（如果多服务部署）
 * @author William
 * @createTime 18-5-29:下午11:50
 * @Description: 通道适配器(用一句话描述该文件做什么)
 */
public class ServerChannelHandlerAdapter extends ChannelHandlerAdapter {
    //存储每一个客户端接入进来的对象
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static Map<String,ChannelId> channelMap = new ConcurrentHashMap<>();

    /**
     *  @param siteCode 站点前缀
     * @param token 用户ID
     * @param channelId netty信道
     */
    public static void addChannelMap(String siteCode, String token, ChannelId channelId) {
        channelMap.put(siteCode+":"+token,channelId);
    }

    /**
     *
     * @param token
     * @param siteCode
     * @return
     */
    public static ChannelId getTargetChannel(String siteCode,String token){
        return channelMap.get(siteCode+":"+token);
    }
}
