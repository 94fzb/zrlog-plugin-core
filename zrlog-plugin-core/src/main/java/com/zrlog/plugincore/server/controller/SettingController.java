package com.zrlog.plugincore.server.controller;


import com.hibegin.http.server.web.Controller;
import com.zrlog.plugincore.server.config.PluginConfig;

import java.util.HashMap;
import java.util.Map;

public class SettingController extends Controller {

    public void load() {
        response.renderJson(PluginConfig.getInstance().getPluginCore().getSetting());
    }

    public void update() {
        Map<String, Object> map = new HashMap<>();
        PluginConfig.getInstance().getPluginCore().getSetting().setDisableAutoDownloadLostFile(request.getParaToBool(
                "disableAutoDownloadLostFile"));
        map.put("code", 0);
        map.put("message", "成功");
        getResponse().renderJson(map);
    }
}
