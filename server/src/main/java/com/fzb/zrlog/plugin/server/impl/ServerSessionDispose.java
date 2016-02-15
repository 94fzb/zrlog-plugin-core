package com.fzb.zrlog.plugin.server.impl;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.ISessionDispose;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.DataMap;
import com.fzb.zrlog.plugin.server.dao.WebSiteDAO;
import com.fzb.zrlog.plugin.type.ActionType;
import flexjson.JSONDeserializer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class ServerSessionDispose implements ISessionDispose {

    @Override
    public void handler(IOSession session, MsgPacket packet) {
        ActionType action = ActionType.valueOf(packet.getMethodStr());
        if (action == ActionType.INIT_CONNECT) {
            Plugin plugin = new JSONDeserializer<Plugin>().deserialize(packet.getDataStr());
            session.setPlugin(plugin);
            DataMap.getPluginInfoMap().put(plugin.getShortName(), plugin);
            DataMap.getPluginMap().put(plugin.getShortName(), session);
            session.sendJsonMsg(new HashMap<>(), action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            System.out.println(DataMap.getPluginInfoMap());
        } else if (action == ActionType.HTTP_FILE) {

        } else if (action == ActionType.GET_WEBSITE) {
            Map map = new JSONDeserializer<Map>().deserialize(packet.getDataStr());
            String[] keys = ((String) map.get("key")).split(",");
            try {
                Map<String, String> response = new HashMap();
                for (String key : keys) {
                    String str = (String) new WebSiteDAO().set("name", key).queryFirst("value");
                    response.put(key, str);
                }
                session.sendJsonMsg(response, action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (action == ActionType.SET_WEBSITE) {
            Map<String, Object> map = new JSONDeserializer<Map>().deserialize(packet.getDataStr());
            Map<String, Object> resultMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Map<String, Object> cond = new HashMap();
                cond.put("name", entry.getKey());
                Map<String, Object> result = new HashMap<>();
                try {
                    result.put("result", new WebSiteDAO().set("value", entry.getValue()).update(cond));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                resultMap.put(entry.getKey(), result);
            }
            session.sendJsonMsg(resultMap, action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);

        }
    }
}

