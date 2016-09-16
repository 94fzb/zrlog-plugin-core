package com.fzb.zrlog.plugin.server.controller;


import com.fzb.common.util.RunConstants;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.config.PluginVO;
import com.fzb.zrlog.plugin.server.type.PluginStatus;
import com.fzb.zrlog.plugin.server.util.HttpMsgUtil;
import com.fzb.zrlog.plugin.server.util.PluginUtil;
import com.fzb.zrlog.plugin.server.util.ServerFreeMarkerReaderHandler;
import com.fzb.zrlog.plugin.type.ActionType;
import com.fzb.zrlog.plugin.type.RunType;
import com.hibegin.http.server.web.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PluginController extends Controller {

    private java.util.logging.Logger LOGGER = LoggerUtil.getLogger(PluginController.class);

    private IOSession getSession() {
        return PluginConfig.getInstance().getIOSessionByPluginName(getRequest().getParaToStr("name"));
    }

    public void install() {
        IOSession session = getSession();
        //
        if (session == null) {
            String pluginName = getRequest().getParaToStr("name");
            PluginUtil.loadPlugin(new File(PluginConfig.getInstance().getPluginFileByName(pluginName)));
        }
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_INSTALL.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
    }

    public void stop() {
        Map<String, Object> map = new HashMap<>();
        if (getSession() != null) {
            String pluginName = getSession().getPlugin().getShortName();
            PluginUtil.stopPlugin(pluginName);
            map.put("code", 0);
            map.put("message", "停止成功");
        } else {
            map.put("code", 1);
            map.put("message", "插件没有启动");
        }
        getResponse().renderJson(map);

    }

    public void start() {
        if (getSession() != null) {
            request.getAttr().put("message", "插件已经在运行了");
            response.renderHtmlStr(new ServerFreeMarkerReaderHandler().render("/templates/message.ftl", getRequest()));
        } else {
            if (RunConstants.runType != RunType.DEV) {
                String pluginName = getRequest().getParaToStr("name");
                PluginUtil.loadPlugin(new File(PluginConfig.getInstance().getPluginFileByName(pluginName)));
                int id = IdUtil.getInt();
                getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_START.name()));
                getResponse().addHeader("Content-Type", "text/html");
                getResponse().write(getSession().getPipeInByMsgId(id), 200);
            } else {
                getResponse().renderHtmlStr("dev ENV");
            }
        }
    }

    /**
     * 得到插件列表
     */
    public void index() {
        List<Plugin> usedPlugins = new ArrayList<>();
        List<Plugin> unusedPlugins = new ArrayList<>();
        List<Plugin> allPlugins = new ArrayList<>();
        for (PluginVO pluginEntry : PluginConfig.getInstance().getAllPluginVO()) {
            PluginStatus pluginStatus = pluginEntry.getStatus();
            if (pluginStatus == null || pluginStatus != PluginStatus.START) {
                unusedPlugins.add(pluginEntry.getPlugin());
            } else {
                usedPlugins.add(pluginEntry.getPlugin());
            }
            allPlugins.add(pluginEntry.getPlugin());
        }
        getRequest().getAttr().put("plugins", allPlugins);
        getRequest().getAttr().put("usedPlugins", usedPlugins);
        getRequest().getAttr().put("unusedPlugins", unusedPlugins);
        getRequest().getAttr().put("pluginVersion", ConfigKit.get("plugin.version", ""));
        response.renderHtmlStr(new ServerFreeMarkerReaderHandler().render("/templates/index.ftl", getRequest()));
    }

    /**
     * 得到插件列表
     */
    public void center() {
        String fullUrl = request.getHeader("Full-Url");
        if (fullUrl == null) {
            request.getAttr().put("from", request.getScheme() + "://" + request.getHeader("Host"));
        } else {
            request.getAttr().put("from", fullUrl.substring(0, fullUrl.lastIndexOf("/")));
        }
        response.renderHtmlStr(new ServerFreeMarkerReaderHandler().render("/templates/center.ftl", getRequest()));
    }

    /**
     *
     */
    public void download() {
        String fileName = getRequest().getParaToStr("pluginName");
        try {
            File path = new File(PluginConfig.getInstance().getPluginBasePath());
            File file = new File(path + "/" + fileName);
            if (!file.exists()) {
                String downloadUrl = getRequest().getParaToStr("host") + "/plugin/download?id=" + getRequest().getParaToInt("id");
                PluginUtil.downloadPlugin(fileName, downloadUrl);
                getRequest().getAttr().put("message", "下载插件成功");
            } else {
                getRequest().getAttr().put("message", "插件已经存在了");
            }
        } catch (Exception e) {
            getRequest().getAttr().put("message", "发生一些错误");
            LOGGER.log(Level.FINER, "download error ", e);
        }
        request.getAttr().put("pluginName", fileName.substring(0, fileName.indexOf(".")));
        response.renderHtmlStr(new ServerFreeMarkerReaderHandler().render("/templates/download.ftl", getRequest()));
    }

    public void uninstall() {
        IOSession session = getSession();
        String pluginName = getRequest().getParaToStr("name");
        if (session != null) {
            session.sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.PLUGIN_UNINSTALL.name()));
        }
        PluginUtil.deletePlugin(pluginName);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "移除成功");
        getResponse().renderJson(map);
    }

    public void service() {
        String name = getRequest().getParaToStr("name");
        if (name != null && !"".equals(name)) {
            IOSession session = PluginConfig.getInstance().getIOSessionByService(name);
            if (session != null) {
                int msgId = session.requestService(name, request.decodeParamMap());
                getResponse().addHeader("Content-Type", "application/json");
                getResponse().write(session.getPipeInByMsgId(msgId));
            } else {
                getResponse().renderCode(404);
            }
        }
    }

    public void setting() {
        getRequest().getAttr().put("setting", PluginConfig.getInstance().getPluginCore().getSetting());
        response.renderHtmlStr(new ServerFreeMarkerReaderHandler().render("/templates/setting.ftl", getRequest()));
    }

    public void settingUpdate() {
        Map<String, Object> map = new HashMap<>();
        PluginConfig.getInstance().getPluginCore().getSetting().setAutoDownloadLostFile(request.getParaToBool("autoDownloadLostFile"));
        map.put("code", 0);
        map.put("message", "成功");
        getResponse().renderJson(map);
    }

    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }
}
