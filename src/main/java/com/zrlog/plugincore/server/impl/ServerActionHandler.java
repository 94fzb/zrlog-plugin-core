package com.zrlog.plugincore.server.impl;

import com.fzb.common.dao.impl.DAO;
import com.google.gson.Gson;
import com.hibegin.common.util.LoggerUtil;
import com.zrlog.plugin.IMsgPacketCallBack;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.api.IActionHandler;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.common.model.CreateArticleRequest;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.common.model.TemplatePath;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.dao.ArticleDAO;
import com.zrlog.plugincore.server.dao.CommentDAO;
import com.zrlog.plugincore.server.dao.TypeDAO;
import com.zrlog.plugincore.server.dao.WebSiteDAO;
import com.zrlog.plugincore.server.type.PluginStatus;
import com.zrlog.plugincore.server.util.HttpUtils;
import com.zrlog.plugincore.server.util.PluginUtil;
import com.zrlog.plugincore.server.util.StringUtils;
import org.jsoup.Jsoup;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerActionHandler implements IActionHandler {

    private static final Logger LOGGER = LoggerUtil.getLogger(ServerActionHandler.class);

    @Override
    public void service(final IOSession session, final MsgPacket msgPacket) {
        handleMassagePackage(session, msgPacket);
    }

    private void handleMassagePackage(final IOSession session, final MsgPacket msgPacket) {
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

    private static void refreshCache(IOSession session) throws Exception {
        if (RunConstants.runType != RunType.BLOG) {
            return;
        }
        Map<String, String> requestHeaders = new HashMap<>();
        String cookie = (String) session.getAttr().get("cookie");
        if (StringUtils.isEmpty(cookie)) {
            return;
        }
        requestHeaders.put("Cookie", cookie);
        HttpUtils.sendGetRequest(session.getAttr().get("accessUrl") + "/api/admin/refreshCache", requestHeaders);
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
            Map<String, String> response = new LinkedHashMap<>();
            for (String key : keys) {
                String name = session.getPlugin().getShortName() + "_" + key;
                Object obj = new WebSiteDAO().set("name", name).queryFirst("value");
                response.put(key, (String) obj);
            }
            session.sendJsonMsg(response, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
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
                LOGGER.log(Level.SEVERE, "", e);
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
                        LOGGER.log(Level.SEVERE, "", e);
                    }
                }
            } else {
                try {
                    new WebSiteDAO().saveOrUpdate("staticResourceHost", "");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                }
            }
        }
        session.sendJsonMsg(resultMap, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        try {
            refreshCache(session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void httpMethod(final IOSession session, final MsgPacket msgPacket) {
        handleMassagePackage(session, msgPacket);
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

    private static boolean convertToBool(Object dbSetting) {
        if (Objects.isNull(dbSetting)) {
            return false;
        }
        if (dbSetting instanceof Boolean) {
            return (boolean) dbSetting;
        }
        return dbSetting instanceof String && ("1".equals(dbSetting) || "on".equals(dbSetting) || Objects.equals(dbSetting, "true"));
    }

    @Override
    public void loadPublicInfo(IOSession session, MsgPacket msgPacket) {
        String[] keys = "title,second_title,home,admin_darkMode,admin_color_primary".split(",");
        try {
            Map<String, String> response = new HashMap<>();
            for (String key : keys) {
                String str = (String) new WebSiteDAO().set("name", key).queryFirst("value");
                response.put(key, str);
            }
            // convert to publicInfo
            PublicInfo publicInfo = new PublicInfo();
            publicInfo.setHomeUrl(response.get("home"));
            publicInfo.setTitle(response.get("title"));
            publicInfo.setSecondTitle(response.get("second_title"));
            publicInfo.setAdminColorPrimary(response.get("admin_color_primary"));
            publicInfo.setDarkMode(convertToBool(response.get("admin_darkMode")));
            session.sendJsonMsg(publicInfo, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
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
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    @Override
    public void getBlogRuntimePath(IOSession session, MsgPacket msgPacket) {
        session.sendJsonMsg(PluginConfig.getInstance().getBlogRunTime(), ActionType.BLOG_RUN_TIME.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    public String getPlainSearchTxt(String content) {
        return Jsoup.parse(content).body().text();
    }

    @Override
    public void createArticle(IOSession session, MsgPacket msgPacket) {
        CreateArticleRequest createArticleRequest = new Gson().fromJson(msgPacket.getDataStr(), CreateArticleRequest.class);
        Integer typeId = 0;
        if (createArticleRequest.getTypeId() > 0) {
            typeId = createArticleRequest.getTypeId();
        } else {
            try {
                typeId = (Integer) new TypeDAO().findByName(createArticleRequest.getType());
                if (typeId == null) {
                    new TypeDAO().set("typeName", createArticleRequest.getType()).set("alias", createArticleRequest.getType()).save();
                    //query again;
                    typeId = (Integer) new TypeDAO().findByName(createArticleRequest.getType());

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String alias = createArticleRequest.getAlias();

        if (alias == null) {
            try {
                alias = new ArticleDAO().queryFirstObj("select max(logId) from log") + "";
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Map<String, Boolean> map = new HashMap<>();
        try {
            Integer logId = (Integer) new ArticleDAO().queryFirstObj("select logId from log where alias = ?", alias);
            DAO articleDAO = new ArticleDAO()
                    .set("releaseTime", new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(createArticleRequest.getReleaseDate()))
                    .set("last_update_date", new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(createArticleRequest.getReleaseDate()))
                    .set("content", createArticleRequest.getContent())
                    .set("title", createArticleRequest.getTitle())
                    .set("markdown", createArticleRequest.getMarkdown())
                    .set("digest", createArticleRequest.getDigest())
                    .set("typeId", typeId)
                    .set("private", createArticleRequest.is_private())
                    .set("rubbish", createArticleRequest.isRubbish())
                    .set("alias", alias)
                    .set("plain_content", getPlainSearchTxt(createArticleRequest.getContent()))
                    .set("thumbnail", createArticleRequest.getThumbnail())
                    .set("canComment", createArticleRequest.isCanComment())
                    .set("recommended", createArticleRequest.isRecommended())
                    .set("keywords", createArticleRequest.getKeywords())
                    .set("editor_type", createArticleRequest.getEditorType())
                    .set("userId", createArticleRequest.getUserId());
            if (logId == null) {
                try {
                    boolean result = articleDAO.save();
                    refreshCache(session);
                    map.put("result", result);
                    session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                } catch (Exception e) {
                    map.put("result", false);
                    LOGGER.log(Level.SEVERE, "save comment error", e);
                    session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            } else {
                try {
                    Map<String, Object> cond = new HashMap<>();
                    cond.put("logId", logId);
                    boolean result = articleDAO.update(cond);
                    refreshCache(session);
                    map.put("result", result);
                    session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                } catch (Exception e) {
                    map.put("result", false);
                    LOGGER.log(Level.SEVERE, "save comment error", e);
                    session.sendJsonMsg(map, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
