package com.fzb.zrlog.plugin.server;

import com.fzb.common.dao.impl.DAO;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.server.config.HttpServerConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import flexjson.JSONDeserializer;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class Start {
    public static void main(String[] args) throws IOException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource(false);
        Properties properties = new Properties();
        try {
            properties.load(Start.class.getResourceAsStream("/db.properties"));
            dataSource.setJdbcUrl(properties.get("jdbcUrl").toString());
            dataSource.setPassword(properties.get("password").toString());
            dataSource.setDriverClass(properties.get("driverClass").toString());
            dataSource.setUser(properties.get("user").toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        DAO.setDs(dataSource);
        new WebServerBuilder.Builder().config(new HttpServerConfig()).build().startWithThread();
        String pluginPath = (args != null && args.length > 0) ? args[0] : "/home/xiaochun/zrlog-plugin/";

        File file = new File(PathKit.getRootPath() + "/plugin.json");
        if(file.exists()){
            List<String> plugins = Files.readAllLines(Paths.get(file.toURI()));
            for (String plugin : plugins) {
                Plugin p = new JSONDeserializer<Plugin>().deserialize(plugin);
                DataMap.getPluginInfoMap().put(p.getShortName(), p);
            }
        }


        ISocketServer socketServer = new NioServer(pluginPath);
        socketServer.create();
        socketServer.listener();
    }
}