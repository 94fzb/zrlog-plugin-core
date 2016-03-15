package com.fzb.zrlog.plugin.server.controller;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.server.Controller;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.server.DataMap;
import com.fzb.zrlog.plugin.server.HttpMsgUtil;
import com.fzb.zrlog.plugin.type.ActionType;

public class PluginController extends Controller {

    private IOSession getSession() {
        return DataMap.getPluginMap().get(getRequest().getParaToStr("name"));
    }

    public void delete() {
        stop();
    }

    public void install() {
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_INSTALL.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
    }

    public void stop() {
        int id = IdUtil.getInt();
        getSession().sendMsg(new MsgPacket(genInfo(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, id, ActionType.PLUGIN_STOP.name()));
        getResponse().addHeader("Content-Type", "text/html");
        getResponse().write(getSession().getPipeInByMsgId(id));
        getSession().close();
    }

    public void start() {
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
        getRequest().getAttr().put("usedPlugins", DataMap.getPluginInfoMap().values());
        getRequest().getAttr().put("unusedPlugins", DataMap.getPluginInfoMap().values());
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
