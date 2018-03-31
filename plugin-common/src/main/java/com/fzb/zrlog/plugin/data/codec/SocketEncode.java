package com.fzb.zrlog.plugin.data.codec;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.type.RunType;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketEncode {

    private static final Logger LOGGER = LoggerUtil.getLogger(SocketEncode.class);

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public void doEncode(IOSession session, MsgPacket msgPacket) throws Exception {
        reentrantLock.lock();
        try {
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
            SocketChannel channel = (SocketChannel) session.getSystemAttr().get("_channel");
            Selector selector = (Selector) session.getSystemAttr().get("_selector");
            //channel.register(selector, SelectionKey.OP_WRITE);
            if (selector.isOpen()) {
                while (sendBuffer.hasRemaining()) {
                    int len = channel.write(sendBuffer);
                    if (len < 0) {
                        throw new EOFException();
                    }
                }
                if (RunConstants.runType == RunType.DEV) {
                    LOGGER.info("send >>> " + session.getAttr().get("count") + " " + msgPacket);
                }
                channel.register(selector, SelectionKey.OP_READ);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        } finally {
            reentrantLock.unlock();
        }
    }
}