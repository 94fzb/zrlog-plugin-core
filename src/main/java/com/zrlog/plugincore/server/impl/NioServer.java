package com.zrlog.plugincore.server.impl;

import com.fzb.net.socket.ISocketServer;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.SocketCodec;
import com.zrlog.plugin.data.codec.SocketDecode;
import com.zrlog.plugin.data.codec.SocketEncode;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.util.PluginUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NioServer implements ISocketServer {


    private static final Logger LOGGER = LoggerUtil.getLogger(NioServer.class);

    private Selector selector;
    private final Map<Socket, IOSession> decoderMap = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(8);

    public NioServer() {
    }

    @Override
    public void listener() {
        if (selector == null) {
            return;
        }
        while (selector.isOpen()) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    SocketChannel channel;
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        channel = server.accept();
                        if (channel != null) {
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        channel = (SocketChannel) key.channel();
                        if (channel != null) {
                            IOSession session = decoderMap.get(channel.socket());
                            if (session == null) {
                                session = new IOSession(channel, selector, new SocketCodec(new SocketEncode(), new SocketDecode(executor)), new ServerActionHandler());
                                decoderMap.put(channel.socket(), session);
                            }
                            dispose(session, channel, key);
                        }
                    }
                    iter.remove();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void create() {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress("127.0.0.1", PluginConfig.getInstance().getMasterPort()));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LOGGER.info("zrlog-plugin-core-server listening on port -> " + PluginConfig.getInstance().getMasterPort());
            PluginUtil.loadPlugins();
            if (RunConstants.runType == RunType.DEV) {
                LOGGER.info("load plugin files");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    @Override
    public void dispose(IOSession session, SocketChannel channel, SelectionKey key) {
        long start = System.currentTimeMillis();
        SocketDecode decode = (SocketDecode) session.getSystemAttr().get("_decode");
        try {
            while (!decode.doDecode(session)) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            key.cancel();
            try {
                channel.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "close channel error " + e.getMessage());
            }
            LOGGER.log(Level.SEVERE, "dispose error " + e.getMessage());
        } finally {
            if (RunConstants.runType == RunType.DEV) {
                LOGGER.info(System.currentTimeMillis() - start + " ms");
            }
        }
    }
}
