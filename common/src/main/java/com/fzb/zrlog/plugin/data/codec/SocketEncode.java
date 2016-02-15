package com.fzb.zrlog.plugin.data.codec;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.LoggerUtil;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class SocketEncode {

    private static final Logger LOGGER = LoggerUtil.getLogger(SocketEncode.class);


    public void doEncode(IOSession session, MsgPacket msgPacket) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(msgPacket.getDataLength() + msgPacket.getMethodLength() + 12);
        sendBuffer.put(msgPacket.getdStart());
        sendBuffer.put(msgPacket.getStatus().getType());
        sendBuffer.put(HexaConversionUtil.intToByteArray(msgPacket.getMsgId()));
        sendBuffer.put(msgPacket.getMethodLength());
        sendBuffer.put(msgPacket.getMethodStr().getBytes());
        sendBuffer.put(HexaConversionUtil.intToByteArray(msgPacket.getDataLength()));
        sendBuffer.put(msgPacket.getContentType().getType());
        msgPacket.getData().flip();
        sendBuffer.put(msgPacket.getData().array());
        sendBuffer.flip();
        try {
            SocketChannel channel = (SocketChannel) session.getSystemAttr().get("_channel");
            Selector selector = (Selector) session.getSystemAttr().get("_selector");
            //channel.register(selector, SelectionKey.OP_WRITE);
            while (sendBuffer.hasRemaining()) {
                int len = channel.write(sendBuffer);
                if (len < 0) {
                    throw new EOFException();
                }
            }
            LOGGER.info("send >>> " + msgPacket);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
