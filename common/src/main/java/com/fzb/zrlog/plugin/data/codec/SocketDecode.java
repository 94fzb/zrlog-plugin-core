package com.fzb.zrlog.plugin.data.codec;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

public class SocketDecode {

    private static final Logger LOGGER = LoggerUtil.getLogger(SocketDecode.class);

    private MsgPacket packet;
    private ByteBuffer header = ByteBuffer.allocate(7);
    private ByteBuffer methodAndLengthAndContentType;
    private static ExecutorService service = new ForkJoinPool();

    public SocketDecode() {
        packet = new MsgPacket();
        packet.setDataLength(-1);
    }

    public MsgPacket getPacket() {
        return packet;
    }

    public void setPacket(MsgPacket packet) {
        this.packet = packet;
    }

    public boolean doDecode(final IOSession session) throws Exception {
        SocketChannel channel = (SocketChannel) session.getSystemAttr().get("_channel");
        //LOGGER.info(channel.getRemoteAddress() + " decoding ");
        boolean flag = false;
        if (channel.isOpen() && !channel.socket().isClosed()) {
            if (packet.getDataLength() == -1) {
                //read header
                if (header.hasRemaining()) {
                    channel.read(header);
                    if (header.hasRemaining()) {
                        return false;
                    }
                    byte[] data = header.array();
                    packet.setStatus(MsgPacketStatus.getMsgPacketStatus(data[1]));
                    packet.setMethodLength(data[6]);
                    packet.setMsgId(HexaConversionUtil.byteArrayToInt(HexaConversionUtil.subByts(data, 2, 4)));
                    methodAndLengthAndContentType = ByteBuffer.allocate(packet.getMethodLength() + 4 + 1);
                }
                //read methodName
                if (methodAndLengthAndContentType != null && methodAndLengthAndContentType.hasRemaining()) {
                    channel.read(methodAndLengthAndContentType);
                    if (methodAndLengthAndContentType.hasRemaining()) {
                        return false;
                    }
                    byte[] data = methodAndLengthAndContentType.array();
                    packet.setMethodStr(new String(HexaConversionUtil.subByts(data, 0, packet.getMethodLength())));
                    packet.setDataLength(HexaConversionUtil.byteArrayToInt(HexaConversionUtil.subByts(data, packet.getMethodLength(), 4)));
                    packet.setContentType(ContentType.getContentType(data[data.length - 1]));
                    ByteBuffer dataBuffer = ByteBuffer.allocate(packet.getDataLength());
                    packet.setData(dataBuffer);
                }
                //read data
                if (packet.getData() != null && packet.getData().hasRemaining()) {
                    channel.read(packet.getData());
                    flag = !packet.getData().hasRemaining();
                }
            } else {
                channel.read(packet.getData());
                flag = !packet.getData().hasRemaining();
            }
            if (flag) {
                LOGGER.info("recv <<< " + session.getAttr().get("count") + " " + packet);
                service.execute(new Thread() {
                    @Override
                    public void run() {
                        session.dispose(packet);
                    }
                });
            }
        }
        return flag;
    }

}
