package com.fzb.zrlog.plugin.server.manager;

import com.fzb.http.server.SimpleServerConfig;
import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.impl.ResponseConfig;
import com.fzb.http.server.impl.ServerConfig;

public class HttpServerConfig extends SimpleServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9089);
        serverConfig.addInterceptor(PluginInterceptor.class);
        return serverConfig;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return null;
    }

    @Override
    public ResponseConfig getResponseConfig() {
        ResponseConfig responseConfig = new ResponseConfig();
        responseConfig.setIsGzip(true);
        return responseConfig;
    }
}
