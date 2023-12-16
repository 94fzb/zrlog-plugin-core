package com.zrlog.plugin.data.codec;

import com.google.gson.Gson;
import com.zrlog.plugin.data.codec.convert.*;

import java.nio.ByteBuffer;

public class MsgPacket {

    private final byte dStart;
    private int msgId;
    private MsgPacketStatus status;
    private ContentType contentType;
    private byte methodLength;
    private String methodStr;
    private ByteBuffer data;
    private int dataLength;

    public MsgPacket(Object data, ContentType contentType, MsgPacketStatus status, int msgId, String methodStr) {
        this();
        this.contentType = contentType;
        this.status = status;
        this.msgId = msgId;
        this.methodStr = methodStr;
        ConvertMsgBody convertMsgBody = null;
        if (contentType == ContentType.BYTE) {
            convertMsgBody = new ByteConvertMsgBody();
        } else if (contentType == ContentType.JSON) {
            convertMsgBody = new JsonConvertMsgBody();
        } else if (contentType == ContentType.HTML) {
            convertMsgBody = new StringConvertMsgBody();
        } else if (contentType == ContentType.FILE) {
            convertMsgBody = new FileConvertMsgBody();
        }
        if (convertMsgBody == null) {
            throw new RuntimeException("not found such convert " + contentType);
        }
        this.data = convertMsgBody.toByteBuffer(data);
        this.dataLength = this.data.array().length;
        this.methodLength = (byte) methodStr.getBytes().length;
    }

    public MsgPacket() {
        dStart = 0x5E;
    }


    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public ByteBuffer getData() {
        return data;
    }

    public String getDataStr() {
        return new String(data.array());
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }


    public byte getdStart() {
        return dStart;
    }

    public byte getMethodLength() {
        return methodLength;
    }

    public void setMethodLength(byte methodLength) {
        this.methodLength = methodLength;
    }

    public String getMethodStr() {
        return methodStr;
    }

    public void setMethodStr(String methodStr) {
        this.methodStr = methodStr;
    }

    public MsgPacketStatus getStatus() {
        return status;
    }

    public void setStatus(MsgPacketStatus status) {
        this.status = status;
    }

    public <T> T convertToClass(Class<T> clazz) {
        return new Gson().fromJson(getDataStr(), clazz);
    }

    @Override
    public String toString() {
        if (contentType == ContentType.JSON) {
            return "MsgPacket{" +
                    "contentType=" + contentType +
                    ", status=" + status.name().toLowerCase() +
                    ", methodStr=" + methodStr +
                    ", data=" + new String(data.array()) +
                    ", dataLength=" + dataLength +
                    '}';
        } else {
            return "MsgPacket{" +
                    "contentType=" + contentType +
                    ", status=" + status.name().toLowerCase() +
                    ", methodStr=" + methodStr +
                    ", data= this file" +
                    ", dataLength=" + dataLength +
                    '}';
        }

    }
}
