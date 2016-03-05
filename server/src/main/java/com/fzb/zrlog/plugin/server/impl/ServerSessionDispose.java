package com.fzb.zrlog.plugin.server.impl;

import com.fzb.http.kit.IOUtil;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.zrlog.plugin.IMsgPacketCallBack;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.ISessionDispose;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.DataMap;
import com.fzb.zrlog.plugin.server.dao.WebSiteDAO;
import com.fzb.zrlog.plugin.type.ActionType;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class ServerSessionDispose implements ISessionDispose {

    private static Logger LOGGER = LoggerUtil.getLogger(ServerSessionDispose.class);

    @Override
    public void handler(final IOSession session, final MsgPacket msgPacket) {
        ActionType action = ActionType.valueOf(msgPacket.getMethodStr());
        if (action == ActionType.INIT_CONNECT) {
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
            session.sendJsonMsg(new HashMap<>(), action.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            StringBuilder stringBuilder = new StringBuilder();
            // save to file
            for (Plugin plugin1 : DataMap.getPluginInfoMap().values()) {
                stringBuilder.append(new JSONSerializer().serialize(plugin1));
            }
            IOUtil.writeBytesToFile(stringBuilder.toString().getBytes(), new File(PathKit.getRootPath() + "/plugin.json"));
        } else if (action == ActionType.HTTP_FILE) {

        } else if (action == ActionType.GET_WEBSITE) {
            System.out.println(msgPacket.getDataStr());
            Map map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
            String[] keys = ((String) map.get("key")).split(",");
            try {
                Map<String, String> response = new HashMap();
                for (String key : keys) {
                    String str = (String) new WebSiteDAO().set("name", session.getPlugin().getShortName() + "_" + key).queryFirst("value");
                    response.put(key, str);
                }
                session.sendJsonMsg(response, action.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (action == ActionType.SET_WEBSITE) {
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
            session.sendJsonMsg(resultMap, action.name(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);

        } else if (action == ActionType.SERVICE) {
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
    }
}

