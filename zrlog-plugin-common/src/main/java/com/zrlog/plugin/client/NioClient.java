package com.zrlog.plugin.client;

import com.hibegin.common.util.IOUtil;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.api.*;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.data.codec.SocketCodec;
import com.zrlog.plugin.data.codec.SocketDecode;
import com.zrlog.plugin.data.codec.SocketEncode;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.IRenderHandler;
import com.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NioClient {

    private static final Logger LOGGER = LoggerUtil.getLogger(NioClient.class);

    private IConnectHandler connectHandler;
    private IRenderHandler renderHandler;
    private IActionHandler actionHandler;

    public NioClient() {

    }

    public NioClient(IActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public NioClient(IConnectHandler connectHandler, IRenderHandler renderHandler) {
        this.connectHandler = connectHandler;
        this.renderHandler = renderHandler;
    }

    public void connectServer(String[] args, List<Class> classList,
                              Class<? extends IPluginAction> pluginAction) throws IOException {
        connectServer(args, classList, pluginAction, new ArrayList<>());
    }

    public void connectServer(String[] args, List<Class> classList,
                              Class<? extends IPluginAction> pluginAction, Class<? extends IPluginService> service) throws IOException {
        List<Class<? extends IPluginService>> serviceList = new ArrayList<>();
        serviceList.add(service);
        connectServer(args, classList, pluginAction, serviceList);
    }

    public void connectServer(String[] args, List<Class> classList,
                              Class<? extends IPluginAction> pluginAction, List<Class<? extends IPluginService>> serviceList) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
        Plugin plugin = new Plugin();
        String propertiesPath = "/plugin.properties";
        InputStream in = NioClient.class.getResourceAsStream(propertiesPath);
        if (in == null) {
            throw new IOException("not found properties file " + propertiesPath);
        }
        Properties properties = new Properties();
        properties.load(in);
        plugin.setVersion(properties.getProperty("version", ""));
        plugin.setName(properties.getProperty("name", ""));
        plugin.setDesc(properties.getProperty("desc", ""));
        if (properties.get("dependentService") != null) {
            plugin.setDependentService(new LinkedHashSet<>(Arrays.asList(properties.get("dependentService").toString().split(","))));
        }
        if (properties.get("paths") != null) {
            plugin.setPaths(new LinkedHashSet<>(Arrays.asList(properties.get("paths").toString().split(","))));
        }
        if (properties.get("actions") != null) {
            plugin.setActions(new LinkedHashSet<>(Arrays.asList(properties.get("actions").toString().split(","))));
        }
        plugin.setShortName(properties.getProperty("shortName", ""));
        plugin.setAuthor(properties.getProperty("author", ""));
        plugin.setIndexPage(properties.getProperty("indexPage", ""));
        InputStream inputStream = NioClient.class.getResourceAsStream("/preview-image.base64");
        if (inputStream != null) {
            try {
                plugin.setPreviewImageBase64(new String(IOUtil.getByteByInputStream(inputStream)));
            } finally {
                inputStream.close();
            }
        } else {
            plugin.setPreviewImageBase64("");
        }
        in.close();
        if (serviceList != null && !serviceList.isEmpty()) {
            for (Class<? extends IPluginService> serviceClass : serviceList) {
                Service service = serviceClass.getAnnotation(Service.class);
                if (service == null) {
                    throw new RuntimeException("forget add @Service in the Class " + serviceClass);
                }
                plugin.getServices().add(service.value());
            }
        }
        //parse args
        int serverPort = ConfigKit.getServerPort();
        if (args != null && args.length > 0) {
            serverPort = Integer.valueOf(args[0]);
        }
        if (args != null && args.length > 1) {
            plugin.setId(args[1]);
        }
        InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", serverPort);
        connectServer(serverAddress, classList, plugin, pluginAction, serviceList);
    }

    private void connectServer(InetSocketAddress serverAddress, List<Class> classList, Plugin plugin, Class<? extends IPluginAction> pluginAction,
                               List<Class<? extends IPluginService>> serviceList) {
        if (actionHandler == null) {
            actionHandler = new ClientActionHandler();
        }
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
                    try {
                        SelectionKey selectionKey = (SelectionKey) iterator.next();
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        if (selectionKey.isConnectable()) {
                            if (channel.isConnectionPending()) {
                                channel.finishConnect();
                            }
                            session = new IOSession(channel, selector, new SocketCodec(new SocketEncode(), new SocketDecode()), actionHandler, renderHandler);
                            session.setPlugin(plugin);
                            session.getAttr().put("_actionClassList", classList);
                            session.getAttr().put("_pluginClass", pluginAction);
                            session.getAttr().put("_pluginServices", serviceList);
                            if (connectHandler != null) {
                                session.getAttr().put("_connectHandle", connectHandler);
                            }
                            session.sendJsonMsg(plugin, ActionType.INIT_CONNECT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
                        } else if (selectionKey.isReadable()) {
                            SocketDecode decode = new SocketDecode();
                            while (!decode.doDecode(session)) ;
                        }
                    } catch (Exception e) {
                        exitPlugin(e);
                    } finally {
                        iterator.remove();
                    }
                }
                //selectionKeys.clear();
            }
        } catch (Exception e) {
            exitPlugin(e);
        }
    }

    private void exitPlugin(Exception e) {
        LOGGER.log(Level.SEVERE, "", e);
        System.exit(1);
    }
}