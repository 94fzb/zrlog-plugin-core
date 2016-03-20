package com.fzb.zrlog.plugin.server.controller;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.server.Controller;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.*;
import com.fzb.zrlog.plugin.type.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginController extends Controller {

    private IOSession getSession() {
        return DataMap.getPluginMap().get(getRequest().getParaToStr("name"));
    }

    public void delete() {
        stop();
    }

    public void install() {
        IOSession session = getSession();
        //
        if (session == null) {
            String pluginName = getRequest().getParaToStr("name");
            PluginConfig.startPlugin(DataMap.getPluginFileMap().get(pluginName), Start.MASTER_PORT);
        }
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_INSTALL.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
    }

    public void stop() {
        /*int id = IdUtil.getInt();*/
        /*getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_STOP.name()));*/
        /*getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));*/
        getSession().close();
        DataMap.getPluginStatusMap().put(getSession().getPlugin().getShortName(), PluginStatus.STOP);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "停止成功");
        getResponse().renderJson(map);
    }

    public void start() {
        String pluginName = getRequest().getParaToStr("name");
        PluginConfig.startPlugin(DataMap.getPluginFileMap().get(pluginName), Start.MASTER_PORT);
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_START.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
    }

    /**
     * 得到插件列表
     */
    public void index() {
        getRequest().getAttr().put("plugins", DataMap.getPluginInfoMap().values());
        List<Plugin> usedPlugins = new ArrayList<>();
        List<Plugin> unusedPlugins = new ArrayList<>();
        for (Map.Entry<String, Plugin> pluginEntry : DataMap.getPluginInfoMap().entrySet()) {
            PluginStatus pluginStatus = DataMap.getPluginStatusMap().get(pluginEntry.getKey());
            if (pluginStatus == null || pluginStatus != PluginStatus.START) {
                unusedPlugins.add(pluginEntry.getValue());
            } else {
                usedPlugins.add(pluginEntry.getValue());
            }
        }
        getRequest().getAttr().put("usedPlugins", usedPlugins);
        getRequest().getAttr().put("unusedPlugins", unusedPlugins);
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/index.ftl"), getRequest()));
    }

    /**
     * 得到插件列表
     */
    public void center() {
        request.getAttr().put("url", request.getHeader("Full-Url"));
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/center.ftl"), getRequest()));
    }

    /**
     *
     */
    public void download() {
        //TODO 
    }

    public void uninstall() {
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_UNINSTALL.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
    }

    public void service() {
        String name = getRequest().getParaToStr("name");
        if (name != null && !"".equals(name)) {
            IOSession session = DataMap.getServiceMap().get(name);
            if (session != null) {
                int msgId = session.requestService(name, request.decodeParamMap());
                getResponse().addHeader("Content-Type", "application/json");
                getResponse().write(session.getPipeInByMsgId(msgId));
            } else {
                getResponse().renderCode(404);
            }
        }
    }

    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }
}
