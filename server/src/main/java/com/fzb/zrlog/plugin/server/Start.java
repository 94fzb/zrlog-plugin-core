package com.fzb.zrlog.plugin.server;

import com.fzb.common.dao.impl.DAO;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.zrlog.plugin.server.impl.NioServer;
import com.fzb.zrlog.plugin.server.manager.HttpServerConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Properties;

public class Start {
    public static void main(String[] args) {
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

        ISocketServer socketServer = new NioServer(pluginPath);
        socketServer.create();
        socketServer.listener();
    }
}