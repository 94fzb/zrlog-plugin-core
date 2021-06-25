package com.zrlog.plugin;

import com.zrlog.plugin.data.codec.MsgPacket;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipeInfo {

    public PipeInfo(MsgPacket requestMsgPackage, MsgPacket responseMsgPacket, PipedInputStream pipedIn, PipedOutputStream pipedOut, IMsgPacketCallBack iMsgPacketCallBack, Long createdAt) {
        this.requestMsgPackage = requestMsgPackage;
        this.responseMsgPacket = responseMsgPacket;
        this.pipedIn = pipedIn;
        this.pipedOut = pipedOut;
        this.iMsgPacketCallBack = iMsgPacketCallBack;
        this.cratedAt = createdAt;
    }

    private MsgPacket requestMsgPackage;
    private MsgPacket responseMsgPacket;
    private PipedInputStream pipedIn;
    private PipedOutputStream pipedOut;
    private IMsgPacketCallBack iMsgPacketCallBack;
    private Long cratedAt;

    public MsgPacket getResponseMsgPacket() {
        return responseMsgPacket;
    }

    public void setResponseMsgPacket(MsgPacket responseMsgPacket) {
        this.responseMsgPacket = responseMsgPacket;
    }

    public MsgPacket getRequestMsgPackage() {
        return requestMsgPackage;
    }

    public void setRequestMsgPackage(MsgPacket requestMsgPackage) {
        this.requestMsgPackage = requestMsgPackage;
    }

    public PipedInputStream getPipedIn() {
        return pipedIn;
    }

    public void setPipedIn(PipedInputStream pipedIn) {
        this.pipedIn = pipedIn;
    }

    public IMsgPacketCallBack getiMsgPacketCallBack() {
        return iMsgPacketCallBack;
    }

    public void setiMsgPacketCallBack(IMsgPacketCallBack iMsgPacketCallBack) {
        this.iMsgPacketCallBack = iMsgPacketCallBack;
    }

    public PipedOutputStream getPipedOut() {
        return pipedOut;
    }

    public void setPipedOut(PipedOutputStream pipedOut) {
        this.pipedOut = pipedOut;
    }

    public Long getCratedAt() {
        return cratedAt;
    }

    public void setCratedAt(Long cratedAt) {
        this.cratedAt = cratedAt;
    }
}