package com.eveb.saasops_msgsender.config;

/**
 * @author William
 * @createTime 18-5-29:下午11:52
 * @Description: Netty服务器常量(用一句话描述该文件做什么)
 */
public class NettyConstanct {

    public static final int port = 7878;
    /**
     * 最大线程量
     */
    public static final int MAX_THREADS = 1024;
    /**
     * 数据包最大长度
     */
    public static final int MAX_FRAME_LENGTH = 65535;

    public static final int WEB_SOCKET_READ_LENGTH = 1024*6;

    public static final int TCP_READ_LENGTH = 1024;

    public static final String REQUEST_PREFIX_SOCKET = "/socket";

    public static final String REQUEST_PREFIX_HTTP = "/http";

    public static final String REQUEST_PREFIX_MyChannel = "/saasops";

    public static final String WEB_SOCKET_URL ="ws://127.0.0.1:"+port+REQUEST_PREFIX_SOCKET;

    public static boolean isMyChannel(String str){
       return str.startsWith(REQUEST_PREFIX_MyChannel);
    }
}
