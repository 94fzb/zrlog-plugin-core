package com.fzb.zrlog.plugin.client;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.api.IPluginAction;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.data.codec.SocketCodec;
import com.fzb.zrlog.plugin.data.codec.SocketDecode;
import com.fzb.zrlog.plugin.data.codec.SocketEncode;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class NioClient {


    public void connectServerByProperties(String[] args, List<Class> classList, String propertiesPath, Class<? extends IPluginAction> pluginAction) throws IOException {
        Plugin request = new Plugin();
        InputStream in = NioClient.class.getResourceAsStream(propertiesPath);
        if (in == null) {
            throw new IOException("not found properties file " + propertiesPath);
        }
        Properties properties = new Properties();
        properties.load(in);
        request.setVersion(properties.getProperty("version", ""));
        request.setName(properties.getProperty("name", ""));
        request.setDesc(properties.getProperty("desc", ""));
        if (properties.get("services") != null) {
            request.setServices(Arrays.asList(properties.get("services").toString().split(",")));
        }
        if (properties.get("paths") != null) {
            request.setPaths(Arrays.asList(properties.get("paths").toString().split(",")));
        }
        if (properties.get("actions") != null) {
            request.setActions(Arrays.asList(properties.get("actions").toString().split(",")));
        }
        request.setShortName(properties.getProperty("shortName", ""));
        request.setAuthor(properties.getProperty("author", ""));
        request.setIndexPage(properties.getProperty("indexPage", ""));
        in.close();
        //parse args
        int serverPort = ConfigKit.getServerPort();
        if (args != null && args.length > 0) {
            serverPort = Integer.valueOf(args[0]);
        }
        InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", serverPort);
        connectServer(serverAddress, classList, request, pluginAction);
    }

    private void connectServer(InetSocketAddress serverAddress, List<Class> classList, Plugin plugin, Class<? extends IPluginAction> pluginAction) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            Iterator iterator;
            Set<SelectionKey> selectionKeys;
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(serverAddress);
            IOSession session = null;
            while (true) {
                selector.select();
                selectionKeys = selector.selectedKeys();
                iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    if (selectionKey.isConnectable()) {
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        session = new IOSession(channel, selector, new SocketCodec(new SocketEncode(), new SocketDecode()), new ClientSessionDispose());
                        session.setPlugin(plugin);
                        session.getAttr().put("_actionClassList", classList);
                        session.getAttr().put("_pluginClass", pluginAction);
                        session.sendJsonMsg(plugin, ActionType.INIT_CONNECT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
                    } else if (selectionKey.isReadable()) {
                        SocketDecode decode = new SocketDecode();
                        while (!decode.doDecode(session)) ;
                    }
                }
                iterator.remove();
                //selectionKeys.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("unknown error exit");
            System.exit(1);
        }
    }
}