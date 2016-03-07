package com.fzb.zrlog.plugin.server.impl;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.data.codec.SocketCodec;
import com.fzb.zrlog.plugin.data.codec.SocketDecode;
import com.fzb.zrlog.plugin.data.codec.SocketEncode;
import com.fzb.zrlog.plugin.server.ISocketServer;
import com.fzb.zrlog.plugin.server.PluginConfig;

import java.io.File;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NioServer implements ISocketServer {


    private static final Logger LOGGER = LoggerUtil.getLogger(NioServer.class);

    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(10);
    private Map<Socket, IOSession> decoderMap = new ConcurrentHashMap<>();
    private String pluginPath;
    private Integer serverPort;

    public NioServer(String pluginPath, Integer serverPort) {
        this.pluginPath = pluginPath;
        this.serverPort = serverPort;
    }

    @Override
    public void listener() {
        if (selector == null) {
            return;
        }
        while (true) {
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
                                session = new IOSession(channel, selector, new SocketCodec(new SocketEncode(), new SocketDecode()), new ServerActionHandler());
                                decoderMap.put(channel.socket(), session);
                            }
                            dispose(session, channel, key);
                        }
                    }
                    iter.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            serverChannel.socket().bind(new InetSocketAddress("127.0.0.1", serverPort));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LOGGER.info("plugin listening on port -> " + serverPort);
            PluginConfig.loadJarPlugin(new File(pluginPath), serverPort);
            LOGGER.info("load jar files");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(IOSession session, SocketChannel channel, SelectionKey key) {
        long start = System.currentTimeMillis();
        SocketDecode decode = new SocketDecode();
        try {
            while (!decode.doDecode(session)) {

            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                key.cancel();
            }
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "dispose error " + e.getMessage());
        } finally {
            LOGGER.info(System.currentTimeMillis() - start + " ms");
        }
    }
}
