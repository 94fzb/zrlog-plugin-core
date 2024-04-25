package com.zrlog.plugincore.server;

import com.fzb.net.socket.ISocketServer;
import com.hibegin.common.util.IOUtil;
import com.hibegin.http.server.WebServerBuilder;
import com.hibegin.http.server.config.ServerConfig;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.modle.BlogRunTime;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginHttpServerConfig;
import com.zrlog.plugincore.server.impl.NioServer;
import com.zrlog.plugincore.server.util.DevUtil;
import com.zrlog.plugincore.server.util.ListenWebServerThread;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class Application {

    private static final Logger LOGGER = LoggerUtil.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
        if (args != null && args.length > 0) {
            LOGGER.info("args = " + Arrays.toString(args));
        }
        Integer serverPort = (args != null && args.length > 0) ? Integer.parseInt(args[0]) : 9089;
        int masterPort = (args != null && args.length > 1) ? Integer.valueOf(args[1]) : ConfigKit.getServerPort();
        String dbProperties = (args != null && args.length > 2) ? args[2] : null;
        String pluginPath = (args != null && args.length > 3) ? args[3] : DevUtil.pluginHome();
        BlogRunTime blogRunTime = new BlogRunTime();
        if (dbProperties == null) {
            File tmpFile = File.createTempFile("blog-db", ".properties");
            dbProperties = tmpFile.toString();
            IOUtil.writeBytesToFile(IOUtil.getByteByInputStream(Application.class.getResourceAsStream("/db.properties")), tmpFile);
            blogRunTime.setPath(DevUtil.blogRuntimePath());
            blogRunTime.setVersion("1.5");
        } else {
            RunConstants.runType = RunType.DEV;
            int port = (args.length > 4) ? Integer.parseInt(args[4]) : -1;
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
        PluginHttpServerConfig config = new PluginHttpServerConfig(serverPort);
        ServerConfig serverConfig = config.getServerConfig();
        serverConfig.setHost("127.0.0.1");
        serverConfig.setDisableSession(true);
        new WebServerBuilder.Builder().config(config).serverConfig(serverConfig).build().startWithThread();
    }
}