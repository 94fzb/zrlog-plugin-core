package com.zrlog.plugin.data.codec;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.HexaConversionUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.type.RunType;

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
            ByteBuffer sendBuffer = ByteBuffer.wrap(HexaConversionUtil.mergeBytes(new byte[]{msgPacket.getdStart()}, new byte[]{msgPacket.getStatus().getType()},
                    HexaConversionUtil.intToByteArray(msgPacket.getMsgId()), new byte[]{msgPacket.getMethodLength()}, msgPacket.getMethodStr().getBytes(),
                    HexaConversionUtil.intToByteArray(msgPacket.getDataLength()), new byte[]{msgPacket.getContentType().getType()}, msgPacket.getData().array()));
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
