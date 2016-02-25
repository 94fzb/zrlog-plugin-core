package com.fzb.zrlog.plugin.server.controller;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.server.Controller;
import com.fzb.zrlog.plugin.server.DataMap;

public class PluginController extends Controller {

    /**
     * 得到插件列表
     */
    public void list() {
        getRequest().getAttr().put("plugins", DataMap.getPluginInfoMap().values());
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/index.ftl"), getRequest()));
    }

    public void delete() {

    }

    public void install() {

    }

    public void stop() {

    }

    public void index() {

    }

    public void uninstall(){

    }
}
