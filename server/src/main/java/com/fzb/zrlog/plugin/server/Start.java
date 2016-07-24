package com.fzb.zrlog.plugin.server;

import com.fzb.common.util.IOUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.http.server.impl.ServerConfig;
import com.fzb.net.socket.ISocketServer;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.common.modle.BlogRunTime;
import com.fzb.zrlog.plugin.server.config.HttpServerConfig;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.server.util.DevUtil;
import com.fzb.zrlog.plugin.server.util.ListenWebServerThread;
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
        String pluginPath = (args != null && args.length > 3) ? args[3] : DevUtil.pluginHome();
        BlogRunTime blogRunTime = new BlogRunTime();
        if (dbProperties == null) {
            File tmpFile = File.createTempFile("blog-db", ".properties");
            dbProperties = tmpFile.toString();
            IOUtil.writeBytesToFile(IOUtil.getByteByInputStream(Start.class.getResourceAsStream("/db.properties")), tmpFile);
            blogRunTime.setPath(DevUtil.blogRuntimePath());
            blogRunTime.setVersion("1.5");
        } else {
            RunConstants.runType = RunType.BLOG;
            int port = (args.length > 4) ? Integer.valueOf(args[4]) : -1;
            blogRunTime.setPath((args.length > 5) ? args[5] : DevUtil.blogRuntimePath());
            blogRunTime.setVersion((args.length > 5) ? args[5] : DevUtil.blogVersion());
            if (port > 0) {
                new ListenWebServerThread(port).start();
            }
        }
        loadHttpServer(serverPort);
        //load Db
        PluginConfig.init(RunConstants.runType, new File(dbProperties), masterPort, pluginPath, blogRunTime);

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