package com.zrlog.plugincore.server.config;

import com.hibegin.http.server.config.AbstractServerConfig;
import com.hibegin.http.server.config.RequestConfig;
import com.hibegin.http.server.config.ResponseConfig;
import com.hibegin.http.server.config.ServerConfig;
import com.hibegin.http.server.web.MethodInterceptor;
import com.zrlog.plugincore.server.controller.PluginApiController;
import com.zrlog.plugincore.server.controller.PluginController;
import com.zrlog.plugincore.server.controller.SettingController;
import com.zrlog.plugincore.server.handle.PluginHandle;

public class PluginHttpServerConfig extends AbstractServerConfig {
    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9089);
        serverConfig.getInterceptors().add(CloseConnectInterceptor.class);
        serverConfig.getInterceptors().add(MethodInterceptor.class);
        serverConfig.addErrorHandle(404, new PluginHandle());
        //real env
        serverConfig.getRouter().addMapper("", PluginController.class);
        serverConfig.addStaticResourceMapper("/static", "/static/static");
        serverConfig.getRouter().addMapper("/api", PluginApiController.class);
        serverConfig.getRouter().addMapper("/api/setting", SettingController.class);
        //dev env
        serverConfig.getRouter().addMapper("/admin/plugins", PluginController.class);
        serverConfig.getRouter().addMapper("/admin/plugins/api", PluginApiController.class);
        serverConfig.getRouter().addMapper("/admin/plugins/setting", SettingController.class);
        serverConfig.addStaticResourceMapper("/admin/plugins/static/css", "/static/static/css");
        serverConfig.addStaticResourceMapper("/admin/plugins/static/js", "/static/static/js");
        return serverConfig;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return null;
    }

    @Override
    public ResponseConfig getResponseConfig() {
        ResponseConfig responseConfig = new ResponseConfig();
        responseConfig.setEnableGzip(false);
        return responseConfig;
    }
}
