package com.eveb.saasops_msgsender.processor;

import org.msgpack.annotation.Message;



@Message
public class MsgBean {

    public MsgBean(String SToken, String token, String msg, String channel) {
        this.SToken = SToken;
        this.token = token;
        this.msg = msg;
        this.channel = channel;
    }

    public MsgBean() {

    }

    private String SToken;
    private String token;
    private String msg;
    private String channel;


    public String getSToken() {
        return SToken;
    }

    public void setSToken(String SToken) {
        this.SToken = SToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "{" +
                "'SToken':'" + SToken + '\'' +
                ", 'token':'" + token + '\'' +
                ", 'msg':'" + msg + '\'' +
                ", 'channel':'" + channel + '\'' +
                '}';
    }
}
