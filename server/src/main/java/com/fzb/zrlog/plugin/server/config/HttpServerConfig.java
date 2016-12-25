package com.fzb.zrlog.plugin.server.config;

import com.fzb.zrlog.plugin.server.controller.PluginController;
import com.fzb.zrlog.plugin.server.incp.PluginInterceptor;
import com.hibegin.http.server.config.AbstractServerConfig;
import com.hibegin.http.server.config.RequestConfig;
import com.hibegin.http.server.config.ResponseConfig;
import com.hibegin.http.server.config.ServerConfig;
import com.hibegin.http.server.web.PluginManagerInterceptor;

import java.util.concurrent.Executors;

public class HttpServerConfig extends AbstractServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9089);
        serverConfig.addInterceptor(PluginManagerInterceptor.class);
        serverConfig.addInterceptor(PluginInterceptor.class);
        serverConfig.getRouter().addMapper("", PluginController.class);
        serverConfig.setExecutor(Executors.newCachedThreadPool());
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
