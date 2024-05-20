package com.zrlog.plugincore.server.controller;

import com.hibegin.http.annotation.ResponseBody;
import com.hibegin.http.server.api.HttpRequest;
import com.hibegin.http.server.api.HttpResponse;
import com.hibegin.http.server.web.Controller;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.util.BooleanUtils;
import com.zrlog.plugincore.server.util.HttpMsgUtil;
import com.zrlog.plugincore.server.util.PluginUtil;
import com.zrlog.plugincore.server.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PluginApiController extends Controller {

    public PluginApiController() {
    }

    public PluginApiController(HttpRequest request, HttpResponse response) {
        super(request, response);
    }

    private IOSession getSession() {
        return PluginConfig.getInstance().getIOSessionByPluginName(getRequest().getParaToStr("name"));
    }

    @ResponseBody
    public Map<String, Object> plugins() {
        List<Plugin> allPlugins = new ArrayList<>();
        for (PluginVO pluginEntry : PluginConfig.getInstance().getAllPluginVO()) {
            if (StringUtils.isEmpty(pluginEntry.getPlugin().getPreviewImageBase64())) {
                pluginEntry.getPlugin().setPreviewImageBase64("");
            }
            allPlugins.add(pluginEntry.getPlugin());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("plugins", allPlugins);
        map.put("dark", BooleanUtils.isTrue(getRequest().getHeader("Dark-Mode")));
        map.put("primaryColor", Objects.requireNonNullElse(getRequest().getHeader("Admin-Color-Primary"), "#1677ff"));
        map.put("pluginVersion", ConfigKit.get("version", ""));
        map.put("pluginBuildId", ConfigKit.get("buildId", ""));
        map.put("pluginBuildNumber", ConfigKit.get("buildNumber", ""));
        String from = request.getHeader("Referer");
        map.put("pluginCenter", "https://store.zrlog.com/plugin/index.html?upgrade-v3=true&from=" + from.substring(0, from.lastIndexOf("/")) + "/plugins");
        return map;
    }

    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }

    @ResponseBody
    public Map<String, Object> stop() {
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
        return map;

    }

    public void start() throws IOException {
        if (getSession() != null) {
            response.redirect("/admin/plugins/pluginStarted");
            return;
        }
        if (RunConstants.runType != RunType.DEV) {
            String pluginName = getRequest().getParaToStr("name");
            PluginUtil.loadPlugin(new File(PluginConfig.getInstance().getPluginFileByName(pluginName)));
            int id = IdUtil.getInt();
            getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id,
                    ActionType.PLUGIN_START.name()));
            getResponse().addHeader("Content-Type", "text/html");
            getResponse().write(getSession().getPipeInByMsgId(id), 200);
        } else {
            getResponse().renderHtmlStr("dev ENV");
        }
    }

    @ResponseBody
    public Map<String, Object> uninstall() {
        IOSession session = getSession();
        String pluginName = getRequest().getParaToStr("name");
        if (session != null) {
            session.sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                    ActionType.PLUGIN_UNINSTALL.name()));
        }
        PluginUtil.deletePlugin(pluginName);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("message", "移除成功");
        return map;
    }
}
