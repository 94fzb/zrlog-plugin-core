package com.zrlog.plugincore.server.controller;


import com.hibegin.common.util.FileUtils;
import com.hibegin.http.server.util.PathUtil;
import com.hibegin.http.server.web.Controller;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;
import com.zrlog.plugincore.server.util.HttpMsgUtil;
import com.zrlog.plugincore.server.util.PluginUtil;

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
            String fileStr = PluginConfig.getInstance().getPluginFileByName(pluginName);
            if (fileStr != null) {
                PluginUtil.loadPlugin(new File(fileStr));
            } else {
                response.renderCode(404);
                return;
            }
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
            response.redirect(getBasePath().substring(0, getBasePath().lastIndexOf("/")) + "/static/plugin-is-start.html");
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
        response.redirect(getBasePath() + "/static/index.html");
    }

    public void plugins() {
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
        response.renderJson(getRequest().getAttr());
    }

    /**
     * 得到插件列表
     */
    public void center() {
        String fullUrl = request.getHeader("Full-Url");
        String from;
        if (fullUrl == null) {
            from = request.getScheme() + "://" + request.getHeader("Host");
        } else {
            from = fullUrl.substring(0, fullUrl.lastIndexOf("/"));
        }
        response.renderHtmlStr("<iframe src='https://store.zrlog.com/plugin/?from=" + from + "' scrolling='no' style='border: 0px' width='100%' height='1200px'></iframe>");
    }

    private String getBasePath() {
        String fullUrl = request.getHeader("Full-Url");
        String basePath;
        if (fullUrl == null) {
            basePath = request.getUrl().substring(0, request.getUrl().lastIndexOf("/"));
        } else {
            if (fullUrl.contains("?")) {
                fullUrl = fullUrl.substring(0, fullUrl.indexOf("?"));
            }
            basePath = fullUrl.substring(0, fullUrl.lastIndexOf("/"));
        }
        return basePath;
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
                File pluginFile = PluginUtil.downloadPlugin(fileName, downloadUrl);
                PluginUtil.loadPlugin(pluginFile);
                getRequest().getAttr().put("message", "下载插件成功");
            } else {
                getRequest().getAttr().put("message", "插件已经存在了");
            }
        } catch (Exception e) {
            getRequest().getAttr().put("message", "发生一些错误");
            LOGGER.log(Level.FINER, "download error ", e);
        }
        String pluginName = fileName.substring(0, fileName.indexOf("."));
        response.redirect(getBasePath() + "/static/download.html?message=" + request.getAttr().get("message") + "&pluginName=" + pluginName);
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
        response.renderJson(PluginConfig.getInstance().getPluginCore().getSetting());
    }

    public void settingUpdate() {
        Map<String, Object> map = new HashMap<>();
        PluginConfig.getInstance().getPluginCore().getSetting().setDisableAutoDownloadLostFile(request.getParaToBool("disableAutoDownloadLostFile"));
        map.put("code", 0);
        map.put("message", "成功");
        getResponse().renderJson(map);
    }

    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }

    public void upload() {
        Map<String, Object> map = new HashMap<>();
        File file = getRequest().getFile("file");
        String finalFile = PathUtil.getStaticPath() + file.getName() + "." + getRequest().getParaToStr("ext");
        FileUtils.moveOrCopyFile(file.toString(), finalFile, true);
        map.put("url", getBasePath() + "/" + new File(finalFile).getName());
        response.renderJson(map);
    }
}
