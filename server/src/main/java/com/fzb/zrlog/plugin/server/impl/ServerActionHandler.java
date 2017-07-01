package com.fzb.zrlog.plugin.server.impl;

import com.fzb.zrlog.plugin.server.dao.CommentDAO;
import com.fzb.common.util.RunConstants;
import com.fzb.common.util.http.HttpUtil;
import com.fzb.common.util.http.handle.HttpStringHandle;
import com.fzb.zrlog.plugin.IMsgPacketCallBack;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.api.IActionHandler;
import com.fzb.zrlog.plugin.common.modle.Comment;
import com.fzb.zrlog.plugin.common.modle.PublicInfo;
import com.fzb.zrlog.plugin.common.modle.TemplatePath;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.dao.WebSiteDAO;
import com.fzb.zrlog.plugin.server.type.PluginStatus;
import com.fzb.zrlog.plugin.server.util.PluginUtil;
import com.fzb.zrlog.plugin.type.ActionType;
import com.fzb.zrlog.plugin.type.RunType;
import com.google.gson.Gson;
import com.hibegin.common.util.LoggerUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerActionHandler implements IActionHandler {

    private static Logger LOGGER = LoggerUtil.getLogger(ServerActionHandler.class);

    @Override
    public void service(final IOSession session, final MsgPacket msgPacket) {
        if (msgPacket.getStatus() == MsgPacketStatus.SEND_REQUEST) {
            Map<String, Object> map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            String name = map.get("name").toString();
            final IOSession serviceSession = PluginConfig.getInstance().getIOSessionByService(name);
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

    private void refreshCache(String url, String cookie) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Cookie", cookie);
        try {
            HttpUtil.sendGetRequest(url, new HttpStringHandle(), requestHeaders);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"",e);
        }
    }

    @Override
    public void initConnect(IOSession session, MsgPacket msgPacket) {
        Plugin plugin = new Gson().fromJson(msgPacket.getDataStr(), Plugin.class);
        session.setPlugin(plugin);
        Map<String, Object> map = new HashMap<>();
        map.put("runType", RunConstants.runType);
        session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        PluginUtil.registerPlugin(session.getPlugin().getId(), PluginStatus.START, session);
    }

    @Override
    public void getFile(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void loadWebSite(IOSession session, MsgPacket msgPacket) {
        Map map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
        String[] keys = ((String) map.get("key")).split(",");
        try {
            Map<String, String> response = new HashMap<>();
            for (String key : keys) {
                String str = (String) new WebSiteDAO().set("name", session.getPlugin().getShortName() + "_" + key).queryFirst("value");
                response.put(key, str);
            }
            session.sendJsonMsg(response, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"",e);
        }
    }

    @Override
    public void setWebSite(IOSession session, MsgPacket msgPacket) {
        Map<String, Object> map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);

        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = session.getPlugin().getShortName() + "_" + entry.getKey();
            Map<String, Object> result = new HashMap<>();
            try {
                result.put("result", new WebSiteDAO().saveOrUpdate(key, entry.getValue()));
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE,"",e);
            }
            resultMap.put(entry.getKey(), result);
        }
        if (map.get("syncTemplate") != null) {
            if ("on".equals(map.get("syncTemplate"))) {
                String accessHost = (String) map.get("host");
                String accessFolder = (String) map.get("folder");
                if (accessHost != null && accessFolder != null) {
                    accessHost = accessFolder + "/" + accessFolder;
                }
                if (accessHost != null) {
                    try {
                        new WebSiteDAO().saveOrUpdate("staticResourceHost", accessHost);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,"",e);
                    }
                }
            } else {
                try {
                    new WebSiteDAO().saveOrUpdate("staticResourceHost", "");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE,"",e);
                }
            }
        }
        session.sendJsonMsg(resultMap, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        if (RunConstants.runType == RunType.BLOG) {
            refreshCache(session.getAttr().get("accessUrl") + "/admin/cleanCache", session.getAttr().get("cookie").toString());

        }
    }

    @Override
    public void httpMethod(final IOSession session, final MsgPacket msgPacket) {
        if (msgPacket.getStatus() == MsgPacketStatus.SEND_REQUEST) {
            Map<String, Object> map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
            String name = map.get("name").toString();
            final IOSession serviceSession = PluginConfig.getInstance().getIOSessionByService(name);
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
        Comment comment = new Gson().fromJson(msgPacket.getDataStr(), Comment.class);
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
        Comment comment = new Gson().fromJson(msgPacket.getDataStr(), Comment.class);
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
                    .set("header", comment.getHeadPortrait())
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
        map.put("dbProperties", PluginConfig.getInstance().getDbPropertiesFile().toString());
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
            LOGGER.log(Level.SEVERE,"",e);
        }
    }

    @Override
    public void getCurrentTemplate(IOSession session, MsgPacket msgPacket) {
        try {
            String templatePath = (String) new WebSiteDAO().set("name", "template").queryFirst("value");
            TemplatePath template = new TemplatePath();
            template.setValue(templatePath);
            session.sendJsonMsg(template, ActionType.CURRENT_TEMPLATE.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"",e);
        }
    }

    @Override
    public void getBlogRuntimePath(IOSession session, MsgPacket msgPacket) {
        session.sendJsonMsg(PluginConfig.getInstance().getBlogRunTime(), ActionType.BLOG_RUN_TIME.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }
}
