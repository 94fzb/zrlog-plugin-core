package com.fzb.zrlog.plugin;

import com.fzb.common.util.IOUtil;
import com.fzb.common.util.Md5Util;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.data.codec.*;
import com.fzb.zrlog.plugin.data.codec.convert.ConvertFileInfo;
import com.fzb.zrlog.plugin.message.Plugin;
import flexjson.JSONSerializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class IOSession {


    private static final Logger LOGGER = LoggerUtil.getLogger(IOSession.class);

    private Map<String, Object> attr = new ConcurrentHashMap<String, Object>();
    private Map<Integer, Object[]> pipeMap = new ConcurrentHashMap<Integer, Object[]>();
    private Map<String, Object> systemAttr = new ConcurrentHashMap<String, Object>();
    private ISessionDispose dispose;
    private Plugin plugin;

    public IOSession(SocketChannel channel, Selector selector, SocketCodec socketCodec, ISessionDispose dispose) {
        systemAttr.put("_channel", channel);
        systemAttr.put("_selector", selector);
        systemAttr.put("_decode", socketCodec.getSocketDecode());
        systemAttr.put("_encode", socketCodec.getSocketEncode());
        systemAttr.put("_sessionDispose", dispose);
        this.dispose = dispose;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public void sendMsg(ContentType contentType, Object data, String methodStr, int msgId, MsgPacketStatus status, IMsgPacketCallBack callBack) {
        MsgPacket msgPacket = new MsgPacket(data, contentType, status, msgId, methodStr);
        sendMsg(msgPacket, callBack);
    }

    public void sendMsg(MsgPacket msgPacket, IMsgPacketCallBack callBack) {
        try {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);
            Object[] inAndOut = new Object[]{in, out, callBack, msgPacket, null};
            pipeMap.put(msgPacket.getMsgId(), inAndOut);
            new SocketEncode().doEncode(this, msgPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(MsgPacket msgPacket) {
        sendMsg(msgPacket, null);
    }

    public void sendJsonMsg(Object data, String method, int id, MsgPacketStatus status) {
        sendMsg(ContentType.JSON, data, method, id, status, null);
    }

    public void sendJsonMsg(Object data, String method, int id, MsgPacketStatus status, IMsgPacketCallBack callBack) {
        sendMsg(ContentType.JSON, data, method, id, status, callBack);
    }

    public void sendFileMsg(File file, String method, int id, MsgPacketStatus status) {
        FileInfo fileInfo = new FileInfo();
        try {
            byte[] fileBytes = IOUtil.getByteByInputStream(new FileInputStream(file));
            FileDesc fileDesc = new FileDesc();
            fileDesc.setFileName(file.getName());
            fileDesc.setFilePath(file.getParent());
            fileInfo.setFileDesc(fileDesc);

            fileInfo.setDataLength(fileBytes.length);
            fileInfo.setFileBytes(fileBytes);
            fileInfo.setMd5sum(Md5Util.MD5(fileBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMsg(ContentType.FILE, ConvertFileInfo.toByteArr(fileInfo), method, id, status, null);
    }

    public void dispose(MsgPacket msgPacket) {
        if (msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS || msgPacket.getStatus() == MsgPacketStatus.RESPONSE_ERROR) {
            PipedOutputStream outputStream = (PipedOutputStream) pipeMap.get(msgPacket.getMsgId())[1];
            IMsgPacketCallBack callBack = (IMsgPacketCallBack) pipeMap.get(msgPacket.getMsgId())[2];
            pipeMap.get(msgPacket.getMsgId())[4] = msgPacket;
            try {
                outputStream.write(msgPacket.getData().array());
                outputStream.close();
                if (callBack != null) {
                    callBack.handler(msgPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dispose.handler(this, msgPacket);
    }

    public void close() {
        try {
            ((Channel) systemAttr.get("_channel")).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getSystemAttr() {
        return systemAttr;
    }

    public Map<String, Object> getAttr() {
        return attr;
    }

    public PipedInputStream getPipeInByMsgId(int msgId) {
        return (PipedInputStream) pipeMap.get(msgId)[0];
    }

    public MsgPacket getRequestMsgPacketByMsgId(int msgId) {
        return (MsgPacket) pipeMap.get(msgId)[3];
    }

    public MsgPacket getResponseMsgPacketByMsgId(int msgId) {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MsgPacket msgPacket = (MsgPacket) pipeMap.get(msgId)[4];
            if (msgPacket != null) {
                return msgPacket;
            }
        }
    }
}

