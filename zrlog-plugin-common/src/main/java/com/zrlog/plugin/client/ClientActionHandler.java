package com.zrlog.plugin.client;

import com.google.gson.Gson;
import com.hibegin.common.util.IOUtil;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.api.*;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class ClientActionHandler implements IActionHandler {

    private static final Logger LOGGER = LoggerUtil.getLogger(ClientActionHandler.class);

    public static String ACTION_NOT_FOUND_PAGE = "<html><body><h1>Not Found</h1></body></html>";

    @Override
    public void service(IOSession session, MsgPacket msgPacket) {
        List<Class<? extends IPluginService>> pluginServices = (List<Class<? extends IPluginService>>) session.getAttr().get("_pluginServices");
        //System.out.println(pluginServices);
        if (pluginServices == null || pluginServices.isEmpty()) {
            session.sendJsonMsg(new HashMap<>(), msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
        } else {
            try {
                if (msgPacket.getContentType() == ContentType.JSON) {
                    Map<String, Object> map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
                    for (Class<? extends IPluginService> serviceClass : pluginServices) {
                        Service service = serviceClass.getAnnotation(Service.class);
                        if (service != null && service.value().equals(map.get("name"))) {
                            serviceClass.newInstance().handle(session, msgPacket);
                            break;
                        } else {
                            System.out.println("service = " + service);
                        }
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "not support the contentType ", msgPacket.getContentType());
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, "handle service method error", e);
            }
        }
    }

    @Override
    public void initConnect(IOSession session, MsgPacket msgPacket) {
        Map<String, Object> map = new Gson().fromJson(msgPacket.getDataStr(), Map.class);
        RunConstants.runType = RunType.valueOf(map.get("runType").toString());
        IConnectHandler connectHandler = (IConnectHandler) session.getAttr().get("_connectHandle");
        if (connectHandler != null) {
            connectHandler.handler(session, msgPacket);
        }
    }

    @Override
    public void getFile(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        InputStream in = ClientActionHandler.class.getResourceAsStream("/templates" + httpRequestInfo.getUri());
        if (in != null) {
            byte[] tmpBytes = IOUtil.getByteByInputStream(in);
            session.sendMsg(ContentType.BYTE, tmpBytes, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS, null);
        } else {
            session.sendMsg(ContentType.BYTE, new byte[]{}, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR, null);
        }
    }

    @Override
    public void loadWebSite(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void setWebSite(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        List<Class> clazzList = (List<Class>) session.getAttr().get("_actionClassList");
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        if (clazzList != null && !clazzList.isEmpty()) {
            for (Class clazz : clazzList) {
                try {
                    Method method = clazz.getMethod(httpRequestInfo.getUri().replace("/", "").replace(".action", ""));
                    Constructor constructor = clazz.getConstructor(IOSession.class, MsgPacket.class, HttpRequestInfo.class);
                    method.invoke(constructor.newInstance(session, msgPacket, httpRequestInfo));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    LOGGER.log(Level.SEVERE, "", e);
                    session.sendMsg(ContentType.HTML, ACTION_NOT_FOUND_PAGE, msgPacket.getMethodStr(), msgPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR, null);
                }
            }
        }
    }

    @Override
    public void deleteComment(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void addComment(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void plugin(IOSession session, MsgPacket msgPacket) {
        ActionType action = ActionType.valueOf(msgPacket.getMethodStr());
        IPluginAction pluginAction = null;
        try {
            pluginAction = ((Class<IPluginAction>) session.getAttr().get("_pluginClass")).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
        if (pluginAction != null) {
            if (action == ActionType.PLUGIN_INSTALL) {
                HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
                pluginAction.install(session, msgPacket, httpRequestInfo);
            } else if (action == ActionType.PLUGIN_START) {
                pluginAction.start(session, msgPacket);
            } else if (action == ActionType.PLUGIN_UNINSTALL) {
                pluginAction.uninstall(session, msgPacket);
                System.exit(1);
            } else if (action == ActionType.PLUGIN_STOP) {
                pluginAction.stop(session, msgPacket);
                System.exit(1);
            }
        }
    }

    @Override
    public void getDbProperties(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void attachment(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void loadPublicInfo(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void getCurrentTemplate(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void getBlogRuntimePath(IOSession session, MsgPacket msgPacket) {

    }

    @Override
    public void createArticle(IOSession session, MsgPacket msgPacket) {

    }
}
