package com.fzb.zrlog.plugin.server;

import com.fzb.common.dao.impl.DAO;
import com.fzb.common.util.RunConstants;
import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.http.server.impl.ServerConfig;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.server.config.HttpServerConfig;
import com.fzb.zrlog.plugin.type.RunType;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import flexjson.JSONDeserializer;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Start {

    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            System.out.println("args = " + Arrays.toString(args));
        }
        Integer serverPort = (args != null && args.length > 0) ? Integer.valueOf(args[0]) : ConfigKit.getServerPort();
        Integer masterPort = (args != null && args.length > 1) ? Integer.valueOf(args[1]) : com.fzb.zrlog.plugin.common.ConfigKit.getServerPort();
        String dbProperties = (args != null && args.length > 2) ? args[2] : null;
        String pluginPath = (args != null && args.length > 3) ? args[3] : "/home/xiaochun/zrlog-plugin";
        //load Db
        initDb(dbProperties);

        loadHttpServer(serverPort);

        DataMap.initData();

        loadPluginServer(pluginPath, masterPort);
    }

    private static void loadPluginServer(String pluginPath, Integer masterPort) {
        ISocketServer socketServer = new NioServer(pluginPath, masterPort);
        socketServer.create();
        socketServer.listener();
    }

    private static void loadHttpServer(Integer serverPort) {
        HttpServerConfig config = new HttpServerConfig();
        ServerConfig serverConfig = config.getServerConfig();
        serverConfig.setPort(serverPort);
        serverConfig.setDisableCookie(true);
        new WebServerBuilder.Builder().config(config).serverConfig(serverConfig).build().startWithThread();
    }


    private static void initDb(String dbProperties) {
        ComboPooledDataSource dataSource = new ComboPooledDataSource(false);
        try {
            Properties properties = new Properties();
            if (dbProperties == null) {
                properties.load(Start.class.getResourceAsStream("/db.properties"));
            } else {
                properties.load(new FileInputStream(dbProperties));
                RunConstants.runType = RunType.BLOG;
            }
            dataSource.setJdbcUrl(properties.get("jdbcUrl").toString());
            dataSource.setPassword(properties.get("password").toString());
            dataSource.setDriverClass(properties.get("driverClass").toString());
            dataSource.setUser(properties.get("user").toString());
        } catch (IOException | PropertyVetoException e) {
            e.printStackTrace();
        }
        DAO.setDs(dataSource);
    }
}