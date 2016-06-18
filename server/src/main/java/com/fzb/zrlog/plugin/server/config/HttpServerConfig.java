package com.fzb.zrlog.plugin.server.config;

import com.fzb.http.server.PluginManagerInterceptor;
import com.fzb.http.server.SimpleServerConfig;
import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.impl.ResponseConfig;
import com.fzb.http.server.impl.ServerConfig;
import com.fzb.zrlog.plugin.server.controller.PluginController;
import com.fzb.zrlog.plugin.server.controller.SettingController;
import com.fzb.zrlog.plugin.server.incp.PluginInterceptor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpServerConfig extends SimpleServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9089);
        serverConfig.addInterceptor(PluginManagerInterceptor.class);
        serverConfig.addInterceptor(PluginInterceptor.class);
        serverConfig.getRouter().addMapper("", PluginController.class);
        serverConfig.setExecutor(Executors.newFixedThreadPool(10));
        return serverConfig;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return null;
    }

    @Override
    public ResponseConfig getResponseConfig() {
        ResponseConfig responseConfig = new ResponseConfig();
        responseConfig.setIsGzip(false);
        return responseConfig;
    }
}
