package com.fzb.zrlog.plugin.server;

import com.fzb.common.util.IOUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.http.server.impl.ServerConfig;
import com.fzb.net.socket.ISocketServer;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.server.config.HttpServerConfig;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.type.RunType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Start {


    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            System.out.println("args = " + Arrays.toString(args));
        }
        Integer serverPort = (args != null && args.length > 0) ? Integer.valueOf(args[0]) : 9089;
        int masterPort = (args != null && args.length > 1) ? Integer.valueOf(args[1]) : ConfigKit.getServerPort();
        String dbProperties = (args != null && args.length > 2) ? args[2] : null;
        String pluginPath = (args != null && args.length > 3) ? args[3] : "/home/xiaochun/zrlog-plugin";
        if (dbProperties == null) {
            File tmpFile = File.createTempFile("blog-db", ".properties");
            dbProperties = tmpFile.toString();
            IOUtil.writeBytesToFile(IOUtil.getByteByInputStream(Start.class.getResourceAsStream("/db.properties")), tmpFile);
        } else {
            RunConstants.runType = RunType.BLOG;
        }
        loadHttpServer(serverPort);
        //load Db
        PluginConfig.init(RunConstants.runType, new File(dbProperties), masterPort, pluginPath);

        loadPluginServer();
    }

    private static void loadPluginServer() {
        ISocketServer socketServer = new NioServer();
        socketServer.create();
        socketServer.listener();
    }

    private static void loadHttpServer(Integer serverPort) {
        HttpServerConfig config = new HttpServerConfig();
        ServerConfig serverConfig = config.getServerConfig();
        serverConfig.setPort(serverPort);
        serverConfig.setHost("127.0.0.1");
        serverConfig.setDisableCookie(true);
        new WebServerBuilder.Builder().config(config).serverConfig(serverConfig).build().startWithThread();
    }
}