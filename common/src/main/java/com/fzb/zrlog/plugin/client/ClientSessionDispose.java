package com.fzb.zrlog.plugin.client;

import com.fzb.common.util.IOUtil;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.ISessionDispose;
import com.fzb.zrlog.plugin.api.IPluginAction;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.type.ActionType;
import flexjson.JSONDeserializer;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class ClientSessionDispose implements ISessionDispose {
    @Override
    public void handler(IOSession session, MsgPacket packet) {
        ActionType action = ActionType.valueOf(packet.getMethodStr());
        if (action == ActionType.HTTP_FILE) {
            HttpRequestInfo httpRequestInfo = new JSONDeserializer<HttpRequestInfo>().deserialize(packet.getDataStr());
            InputStream in = ClientSessionDispose.class.getResourceAsStream("/templates" + httpRequestInfo.getUri());
            if (in != null) {
                byte tmpBytes[] = IOUtil.getByteByInputStream(in);
                session.sendMsg(ContentType.BYTE, tmpBytes, action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS, null);
            } else {
                session.sendMsg(ContentType.BYTE, new byte[]{}, action.name(), packet.getMsgId(), MsgPacketStatus.RESPONSE_ERROR, null);
            }
        } else if (action == ActionType.HTTP_METHOD) {
            List<Class> clazzList = (List<Class>) session.getAttr().get("_actionClassList");
            HttpRequestInfo httpRequestInfo = new JSONDeserializer<HttpRequestInfo>().deserialize(packet.getDataStr());
            if (clazzList != null && !clazzList.isEmpty()) {
                for (Class clazz : clazzList) {
                    try {
                        Method method = clazz.getMethod(httpRequestInfo.getUri().replace("/", "").replace(".action", ""));
                        Constructor constructor = clazz.getConstructor(IOSession.class, MsgPacket.class, HttpRequestInfo.class);
                        method.invoke(constructor.newInstance(session, packet, httpRequestInfo));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (action.name().startsWith("PLUGIN")) {
            IPluginAction pluginAction = null;
            try {
                pluginAction = ((Class<IPluginAction>) session.getAttr().get("_pluginClass")).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (pluginAction != null) {
                if (action == ActionType.PLUGIN_INSTALL) {
                    HttpRequestInfo httpRequestInfo = new JSONDeserializer<HttpRequestInfo>().deserialize(packet.getDataStr());
                    pluginAction.install(session, packet, httpRequestInfo);
                } else if (action == ActionType.PLUGIN_START) {
                    pluginAction.start(session, packet);
                } else if (action == ActionType.PLUGIN_UNINSTALL) {
                    pluginAction.uninstall(session, packet);
                } else if (action == ActionType.PLUGIN_STOP) {
                    pluginAction.stop(session, packet);
                }
            }
        }
    }
}
