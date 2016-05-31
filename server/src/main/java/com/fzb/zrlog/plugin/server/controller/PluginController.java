package com.fzb.zrlog.plugin.server.controller;

import com.fzb.common.util.IOUtil;
import com.fzb.common.util.http.HttpUtil;
import com.fzb.common.util.http.handle.HttpFileHandle;
import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.server.Controller;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.*;
import com.fzb.zrlog.plugin.type.ActionType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PluginController extends Controller {

    private java.util.logging.Logger LOGGER = LoggerUtil.getLogger(PluginController.class);

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
        Map<String, Object> map = new HashMap<>();
        if (getSession() != null) {
            String pluginName = getSession().getPlugin().getShortName();
            getSession().close();
            DataMap.getPluginMap().remove(pluginName);
            DataMap.getPluginStatusMap().put(pluginName, PluginStatus.STOP);
            DataMap.getFilePluginStatusMap().put(DataMap.getPluginFileMap().get(pluginName).toString(), PluginStatus.STOP);
            PluginConfig.closeProcess(pluginName);
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
            response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/message.ftl"), getRequest()));
        } else {
            String pluginName = getRequest().getParaToStr("name");
            PluginConfig.startPlugin(DataMap.getPluginFileMap().get(pluginName), Start.MASTER_PORT);
            int id = IdUtil.getInt();
            getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_START.name()));
            getResponse().addHeader("Content-Type", "text/html");
            getResponse().write(getSession().getPipeInByMsgId(id), 200);
        }
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
        getRequest().getAttr().put("pluginVersion", ConfigKit.get("plugin.version", ""));
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/index.ftl"), getRequest()));
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
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/center.ftl"), getRequest()));
    }

    /**
     *
     */
    public void download() {
        String fileName = getRequest().getParaToStr("pluginName");
        try {
            File path = new File(Start.getPluginBasePath());
            File file = new File(path + "/" + fileName);
            if (!file.exists()) {
                LOGGER.info("download plugin " + fileName);
                HttpFileHandle fileHandle = (HttpFileHandle) HttpUtil.sendGetRequest(getRequest().getParaToStr("host") + "/plugin/download?id=" + getRequest().getParaToInt("id"),
                        new HttpFileHandle(Start.getPluginBasePath()), new HashMap<String, String>());
                String target = fileHandle.getT().getParent() + "/" + fileName;
                IOUtil.moveOrCopyFile(fileHandle.getT().toString(), target, true);
                getRequest().getAttr().put("message", "下载插件成功");
            } else {
                getRequest().getAttr().put("message", "插件已经存在了");
            }
        } catch (Exception e) {
            getRequest().getAttr().put("message", "发生一些错误");
            LOGGER.log(Level.FINER, "download error ", e);
        }
        request.getAttr().put("pluginName", fileName.substring(0, fileName.indexOf(".")));
        response.renderHtmlStr(FreeMarkerKit.renderToFM(PluginController.class.getResourceAsStream("/templates/download.ftl"), getRequest()));
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
