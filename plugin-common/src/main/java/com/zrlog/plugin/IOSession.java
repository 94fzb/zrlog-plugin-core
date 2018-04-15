package com.zrlog.plugin;

import com.hibegin.common.util.IOUtil;
import com.zrlog.plugin.api.IActionHandler;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.*;
import com.zrlog.plugin.data.codec.convert.JsonConvertMsgBody;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.IRenderHandler;
import com.zrlog.plugin.type.ActionType;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.Channel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOSession {


    private static final Logger LOGGER = LoggerUtil.getLogger(IOSession.class);

    private Map<String, Object> attr = new ConcurrentHashMap<>();
    private Map<Integer, Object[]> pipeMap = new ConcurrentHashMap<>();
    private Map<String, Object> systemAttr = new ConcurrentHashMap<>();
    private IActionHandler actionHandler;
    private Plugin plugin;
    private AtomicInteger msgIds = new AtomicInteger();
    private MsgPacketDispose msgPacketDispose = new MsgPacketDispose();
    private IRenderHandler renderHandler;
    private SocketEncode socketEncode;
    private ClearIdlMsgPacket clearIdlMsgPacket;

    public IOSession(SocketChannel channel, Selector selector, SocketCodec socketCodec, IActionHandler actionHandler, IRenderHandler renderHandler) {
        systemAttr.put("_channel", channel);
        systemAttr.put("_selector", selector);
        systemAttr.put("_decode", socketCodec.getSocketDecode());
        systemAttr.put("_encode", socketCodec.getSocketEncode());
        systemAttr.put("_actionHandler", actionHandler);
        this.socketEncode = socketCodec.getSocketEncode();
        this.actionHandler = actionHandler;
        this.renderHandler = renderHandler;
        this.clearIdlMsgPacket = new ClearIdlMsgPacket(pipeMap);
        this.clearIdlMsgPacket.start();
    }

    public IOSession(SocketChannel channel, Selector selector, SocketCodec socketCodec, IActionHandler actionHandler) {
        this(channel, selector, socketCodec, actionHandler, null);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public <T> T getResponseSync(ContentType contentType, Object data, ActionType actionType, Class<T> clazz) {
        int msgId = IdUtil.getInt();
        MsgPacketStatus status = MsgPacketStatus.SEND_REQUEST;
        MsgPacket msgPacket = new MsgPacket(data, contentType, status, msgId, actionType.name());
        sendMsg(msgPacket);
        MsgPacket response = getResponseMsgPacketByMsgId(msgId);
        if (response.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS) {
            if (response.getContentType() == ContentType.JSON) {
                return new JsonConvertMsgBody().toObj(response.getData(), clazz);
            }
        } else {
            throw new RuntimeException("some error");
        }
        throw new RuntimeException("unSupport response " + response.getContentType());
    }

    public void sendMsg(ContentType contentType, Object data, String methodStr, int msgId, MsgPacketStatus status, IMsgPacketCallBack callBack) {
        MsgPacket msgPacket = new MsgPacket(data, contentType, status, msgId, methodStr);
        sendMsg(msgPacket, callBack);
    }

    public void sendMsg(ContentType contentType, Object data, String methodStr, int msgId, MsgPacketStatus status) {
        MsgPacket msgPacket = new MsgPacket(data, contentType, status, msgId, methodStr);
        sendMsg(msgPacket, null);
    }

    public void sendMsg(MsgPacket msgPacket, IMsgPacketCallBack callBack) {
        try {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);
            Object[] inAndOut = new Object[]{in, out, callBack, msgPacket, null, System.currentTimeMillis()};
            pipeMap.put(msgPacket.getMsgId(), inAndOut);
            getAttr().put("count", msgIds.incrementAndGet());
            socketEncode.doEncode(this, msgPacket);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
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

    public void responseHtml(String templatePath, Map dataMap, String method, int id, IMsgPacketCallBack callBack) {
        if (renderHandler != null) {
            sendMsg(ContentType.HTML, renderHandler.render(templatePath, getPlugin(), dataMap), method, id, MsgPacketStatus.RESPONSE_SUCCESS, callBack);
        } else {
            sendMsg(ContentType.HTML, IOUtil.getStringInputStream(IOSession.class.getResourceAsStream(templatePath)), method, id, MsgPacketStatus.RESPONSE_SUCCESS, callBack);
        }
    }

    public void responseHtmlStr(String htmlString, String method, int id) {
        sendMsg(ContentType.HTML, htmlString, method, id, MsgPacketStatus.RESPONSE_SUCCESS, null);
    }

    public void responseHtmlStr(String htmlString, String method, int id, IMsgPacketCallBack callBack) {
        sendMsg(ContentType.HTML, htmlString, method, id, MsgPacketStatus.RESPONSE_SUCCESS, callBack);
    }

    public void responseHtml(String templatePath, Map dataMap, String method, int id) {
        responseHtml(templatePath, dataMap, method, id, null);
    }

    public void sendFileMsg(File file, int id, MsgPacketStatus status) {
        sendMsg(ContentType.FILE, file, ActionType.HTTP_ATTACHMENT_FILE.name(), id, status, null);
    }

    public int requestService(String name, Map map, IMsgPacketCallBack msgPacketCallBack) {
        int msgId = IdUtil.getInt();
        map.put("name", name);
        MsgPacket msgPacket = new MsgPacket(map, ContentType.JSON, MsgPacketStatus.SEND_REQUEST, msgId, ActionType.SERVICE.name());
        sendMsg(msgPacket, msgPacketCallBack);
        return msgId;
    }

    public int requestService(String name, Map map) {
        return requestService(name, map, null);
    }

    public void dispose(MsgPacket msgPacket) {
        try {
            if (msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS || msgPacket.getStatus() == MsgPacketStatus.RESPONSE_ERROR) {
                PipedOutputStream outputStream = (PipedOutputStream) pipeMap.get(msgPacket.getMsgId())[1];
                IMsgPacketCallBack callBack = (IMsgPacketCallBack) pipeMap.get(msgPacket.getMsgId())[2];
                pipeMap.get(msgPacket.getMsgId())[4] = msgPacket;
                outputStream.write(msgPacket.getData().array());
                outputStream.close();
                if (callBack != null) {
                    callBack.handler(msgPacket);
                    // 不进行多次处理
                    return;
                }
            }
            msgPacketDispose.handler(this, msgPacket, actionHandler);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "handle error", e);
        }
    }

    public void close() {
        clearIdlMsgPacket.interrupt();
        try {
            ((Channel) systemAttr.get("_channel")).close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
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
                LOGGER.log(Level.SEVERE, "", e);
            }
            MsgPacket msgPacket = (MsgPacket) pipeMap.get(msgId)[4];
            if (msgPacket != null) {
                return msgPacket;
            }
        }
    }
}

