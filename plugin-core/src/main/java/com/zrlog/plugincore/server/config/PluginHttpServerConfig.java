package com.zrlog.plugincore.server.config;

import com.hibegin.http.server.config.AbstractServerConfig;
import com.hibegin.http.server.config.RequestConfig;
import com.hibegin.http.server.config.ResponseConfig;
import com.hibegin.http.server.config.ServerConfig;
import com.hibegin.http.server.web.PluginManagerInterceptor;
import com.zrlog.plugincore.server.controller.PluginController;
import com.zrlog.plugincore.server.incp.PluginInterceptor;

public class PluginHttpServerConfig extends AbstractServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9089);
        serverConfig.addInterceptor(PluginManagerInterceptor.class);
        serverConfig.addInterceptor(PluginInterceptor.class);
        serverConfig.getRouter().addMapper("", PluginController.class);
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
