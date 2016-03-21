package com.fzb.zrlog.plugin.server.impl;

import com.fzb.common.dao.impl.CommentDAO;
import com.fzb.common.util.RunConstants;
import com.fzb.http.kit.IOUtil;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.zrlog.plugin.IMsgPacketCallBack;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.api.IActionHandler;
import com.fzb.zrlog.plugin.common.modle.Comment;
import com.fzb.zrlog.plugin.common.modle.PublicInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.DataMap;
import com.fzb.zrlog.plugin.server.PluginStatus;
import com.fzb.zrlog.plugin.server.dao.WebSiteDAO;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerActionHandler implements IActionHandler {

    private static Logger LOGGER = LoggerUtil.getLogger(ServerActionHandler.class);

    @Override
    public void service(final IOSession session, final MsgPacket msgPacket) {
        if (msgPacket.getStatus() == MsgPacketStatus.SEND_REQUEST) {
            Map<String, Object> map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
            String name = map.get("name").toString();
            final IOSession serviceSession = DataMap.getServiceMap().get(name);
            if (serviceSession != null) {
                // 消息中转
                serviceSession.requestService(name, map, new IMsgPacketCallBack() {
                    @Override
                    public void handler(MsgPacket responseMsgPacket) {
                        responseMsgPacket.setMsgId(msgPacket.getMsgId());
                        session.sendMsg(responseMsgPacket);
                    }
                });
            } else {
                // not found service response error
                Map<String, Object> response = new HashMap<>();
                response.put("status", 500);
                session.sendJsonMsg(response, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        }
    }

    @Override
    public void initConnect(IOSession session, MsgPacket msgPacket) {
        Plugin plugin = new JSONDeserializer<Plugin>().deserialize(msgPacket.getDataStr());
        session.setPlugin(plugin);
        DataMap.getPluginInfoMap().put(plugin.getShortName(), plugin);
        DataMap.getPluginMap().put(plugin.getShortName(), session);
        for (String serviceName : plugin.getServices()) {
            if (DataMap.getServiceMap().containsKey(serviceName)) {
                LOGGER.log(Level.WARNING, "exists service ", serviceName);
            }
            DataMap.getServiceMap().put(serviceName, session);
        }
        for (String actionName : plugin.getActions()) {
            if (DataMap.getActionMap().containsKey(actionName)) {
                LOGGER.log(Level.WARNING, "exists actionPath ", actionName);
            }
            DataMap.getServiceMap().put(actionName, session);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("runType", RunConstants.runType);
        session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        DataMap.getPluginStatusMap().put(plugin.getShortName(), PluginStatus.START);
    }

    @Override
    public void getFile(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void loadWebSite(IOSession session, MsgPacket msgPacket) {
        Map map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
        String[] keys = ((String) map.get("key")).split(",");
        try {
            Map<String, String> response = new HashMap();
            for (String key : keys) {
                String str = (String) new WebSiteDAO().set("name", session.getPlugin().getShortName() + "_" + key).queryFirst("value");
                response.put(key, str);
            }
            session.sendJsonMsg(response, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWebSite(IOSession session, MsgPacket msgPacket) {
        Map<String, Object> map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = session.getPlugin().getShortName() + "_" + entry.getKey();
            Map<String, Object> cond = new HashMap();
            cond.put("name", key);
            Map<String, Object> result = new HashMap<>();
            try {
                Object object = new WebSiteDAO().set("name", key).queryFirst("value");
                if (object != null) {
                    result.put("result", new WebSiteDAO().set("value", entry.getValue()).update(cond));
                } else {
                    result.put("result", new WebSiteDAO().set("name", key).set("value", entry.getValue()).save());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            resultMap.put(entry.getKey(), result);
        }
        session.sendJsonMsg(resultMap, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    @Override
    public void httpMethod(final IOSession session, final MsgPacket msgPacket) {
        if (msgPacket.getStatus() == MsgPacketStatus.SEND_REQUEST) {
            Map<String, Object> map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
            String name = map.get("name").toString();
            final IOSession serviceSession = DataMap.getServiceMap().get(name);
            if (serviceSession != null) {
                // 消息中转
                serviceSession.requestService(name, map, new IMsgPacketCallBack() {
                    @Override
                    public void handler(MsgPacket responseMsgPacket) {
                        responseMsgPacket.setMsgId(msgPacket.getMsgId());
                        session.sendMsg(responseMsgPacket);
                    }
                });
            } else {
                // not found service response error
                Map<String, Object> response = new HashMap<>();
                response.put("status", 500);
                session.sendJsonMsg(response, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        }
    }

    @Override
    public void deleteComment(IOSession session, MsgPacket msgPacket) {
        Comment comment = new JSONDeserializer<Comment>().deserialize(msgPacket.getDataStr());
        Map<String, Boolean> map = new HashMap<>();
        if (comment.getPostId() != null) {
            try {
                boolean result = new CommentDAO().set("postId", comment.getPostId()).delete();
                map.put("result", result);
                session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            } catch (SQLException e) {
                map.put("result", false);
                LOGGER.log(Level.SEVERE, "delete comment error", e);
                session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        }
    }

    @Override
    public void addComment(IOSession session, MsgPacket msgPacket) {
        Comment comment = new JSONDeserializer<Comment>().deserialize(msgPacket.getDataStr());
        Map<String, Boolean> map = new HashMap<>();
        try {
            boolean result = new CommentDAO()
                    .set("userHome", comment.getHome())
                    .set("userMail", comment.getMail())
                    .set("userIp", comment.getIp())
                    .set("userName", comment.getName())
                    .set("logId", comment.getLogId())
                    .set("postId", comment.getPostId())
                    .set("userComment", comment.getContent())
                    .set("commTime", comment.getCreatedTime())
                    .set("td", new Date())
                    .set("hide", 1).save();

            map.put("result", result);
            session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            map.put("result", false);
            LOGGER.log(Level.SEVERE, "save comment error", e);
            session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
        }
    }

    @Override
    public void plugin(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void getDbProperties(IOSession session, MsgPacket msgPacket) {
        Map<String, Object> map = new HashMap<>();
        map.put("dbProperties", DataMap.getDbProperties().toString());
        session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    @Override
    public void attachment(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void loadPublicInfo(IOSession session, MsgPacket msgPacket) {
        String[] keys = "title,second_title,home".split(",");
        try {
            Map<String, String> response = new HashMap();
            for (String key : keys) {
                String str = (String) new WebSiteDAO().set("name", key).queryFirst("value");
                response.put(key, str);
            }
            // convert to publicInfo
            PublicInfo publicInfo = new PublicInfo();
            publicInfo.setHomeUrl(response.get("home"));
            publicInfo.setTitle(response.get("title"));
            publicInfo.setSecondTitle(response.get("second_title"));
            session.sendJsonMsg(publicInfo, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
