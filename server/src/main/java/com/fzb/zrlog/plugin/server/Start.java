package com.fzb.zrlog.plugin.server;

import com.fzb.common.dao.impl.DAO;
import com.fzb.common.util.IOUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.http.server.impl.ServerConfig;
import com.fzb.zrlog.plugin.server.config.HttpServerConfig;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.type.RunType;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class Start {

    public static Integer MASTER_PORT = 0;
    private static String PLUGIN_BASE_PATH;

    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            System.out.println("args = " + Arrays.toString(args));
        }
        Integer serverPort = (args != null && args.length > 0) ? Integer.valueOf(args[0]) : 9089;
        MASTER_PORT = (args != null && args.length > 1) ? Integer.valueOf(args[1]) : com.fzb.zrlog.plugin.common.ConfigKit.getServerPort();
        String dbProperties = (args != null && args.length > 2) ? args[2] : null;
        String pluginPath = (args != null && args.length > 3) ? args[3] : "/home/xiaochun/zrlog-plugin";
        if (dbProperties == null) {
            File tmpFile = File.createTempFile("blog-db", ".properties");
            dbProperties = tmpFile.toString();
            IOUtil.writeBytesToFile(IOUtil.getByteByInputStream(Start.class.getResourceAsStream("/db.properties")), tmpFile);
        } else {
            RunConstants.runType = RunType.BLOG;
        }
        //load Db
        initDb(dbProperties);

        loadHttpServer(serverPort);

        DataMap.initData(dbProperties);

        loadPluginServer(pluginPath, MASTER_PORT);
    }

    private static void loadPluginServer(String pluginPath, Integer masterPort) {
        PLUGIN_BASE_PATH = pluginPath;
        new File(PLUGIN_BASE_PATH).mkdir();
        ISocketServer socketServer = new NioServer(pluginPath, masterPort);
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


    private static void initDb(String dbProperties) {
        ComboPooledDataSource dataSource = new ComboPooledDataSource(false);
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(dbProperties));
            dataSource.setJdbcUrl(properties.get("jdbcUrl").toString() + "&autoReconnect=true");
            dataSource.setPassword(properties.get("password").toString());
            dataSource.setDriverClass(properties.get("driverClass").toString());
            dataSource.setUser(properties.get("user").toString());
        } catch (IOException | PropertyVetoException e) {
            e.printStackTrace();
        }
        DAO.setDs(dataSource);
    }

    public static String getPluginBasePath() {
        return PLUGIN_BASE_PATH;
    }
}