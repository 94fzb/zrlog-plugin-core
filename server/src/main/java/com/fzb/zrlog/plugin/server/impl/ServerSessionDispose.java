package com.fzb.zrlog.plugin.server.impl;

import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.IOUtil;
import com.fzb.http.kit.PathKit;
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
            StringBuilder stringBuilder = new StringBuilder();
            // save to file
            for (Plugin plugin1 : DataMap.getPluginInfoMap().values()) {
                stringBuilder.append(new JSONSerializer().serialize(plugin1));
            }
            IOUtil.writeBytesToFile(stringBuilder.toString().getBytes(), new File(PathKit.getRootPath() + "/plugin.json"));
        } else if (action == ActionType.HTTP_FILE) {

        } else if (action == ActionType.GET_WEBSITE) {
            System.out.println(packet.getDataStr());
            Map map = new JSONDeserializer<Map>().deserialize(packet.getDataStr());
            String[] keys = ((String) map.get("key")).split(",");
            try {
                Map<String, String> response = new HashMap();
                for (String key : keys) {
                    String str = (String) new WebSiteDAO().set("name", session.getPlugin().getShortName() + "#" + key).queryFirst("value");
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
                String key = session.getPlugin().getShortName() + "#" + entry.getKey();
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
            session.sendJsonMsg(resultMap, action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);

        }
    }
}

