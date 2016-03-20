package com.fzb.zrlog.plugin;

import com.fzb.zrlog.plugin.api.IActionHandler;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.type.ActionType;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class MsgPacketDispose {

    public void handler(final IOSession session, final MsgPacket msgPacket, IActionHandler actionHandler) {
        ActionType action = ActionType.valueOf(msgPacket.getMethodStr());
        if (action == ActionType.INIT_CONNECT) {
            actionHandler.initConnect(session, msgPacket);
        } else if (action == ActionType.HTTP_FILE) {
            actionHandler.getFile(session, msgPacket);
        } else if (action == ActionType.GET_WEBSITE) {
            actionHandler.loadWebSite(session, msgPacket);
        } else if (action == ActionType.SET_WEBSITE) {
            actionHandler.setWebSite(session, msgPacket);
        } else if (action == ActionType.ADD_COMMENT) {
            actionHandler.addComment(session, msgPacket);
        } else if (action == ActionType.SERVICE) {
            actionHandler.service(session, msgPacket);
        } else if (action.name().startsWith("PLUGIN")) {
            actionHandler.plugin(session, msgPacket);
        } else if (action == ActionType.HTTP_METHOD) {
            actionHandler.httpMethod(session, msgPacket);
        } else if (action == ActionType.DELETE_COMMENT) {
            actionHandler.deleteComment(session, msgPacket);
        } else if (action == ActionType.GET_DB_PROPERTIES) {
            actionHandler.getDbProperties(session, msgPacket);
        } else if (action == ActionType.HTTP_ATTACHMENT_FILE) {
            actionHandler.attachment(session, msgPacket);
        } else {
            System.err.println("UnSupport Method " + action);
        }
    }
}

