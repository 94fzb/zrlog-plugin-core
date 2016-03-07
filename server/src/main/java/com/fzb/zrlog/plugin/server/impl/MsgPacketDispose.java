package com.fzb.zrlog.plugin.server.impl;

import com.fzb.http.kit.LoggerUtil;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.api.IActionHandler;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.type.ActionType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class MsgPacketDispose {

    private static Logger LOGGER = LoggerUtil.getLogger(MsgPacketDispose.class);

    public void handler(final IOSession session, final MsgPacket msgPacket, IActionHandler actionHandler) {
        ActionType action = ActionType.valueOf(msgPacket.getMethodStr());
        IActionHandler handler = new ServerActionHandler();
        if (action == ActionType.INIT_CONNECT) {
            handler.initConnect(session, msgPacket);
        } else if (action == ActionType.HTTP_FILE) {
            handler.getFile(session, msgPacket);
        } else if (action == ActionType.GET_WEBSITE) {
            handler.loadWebSite(session, msgPacket);
        } else if (action == ActionType.SET_WEBSITE) {
            handler.setWebSite(session, msgPacket);
        } else if (action == ActionType.ADD_COMMENT) {
            actionHandler.addComment(session, msgPacket);
        } else if (action == ActionType.SERVICE) {
            actionHandler.service(session, msgPacket);
        } else {
            LOGGER.log(Level.WARNING, "UnSupport Method " + action);
        }
    }
}

