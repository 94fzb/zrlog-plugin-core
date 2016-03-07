package com.fzb.zrlog.plugin.api;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;

public interface IActionHandler {

    void service(final IOSession session, final MsgPacket msgPacket);

    void initConnect(final IOSession session, final MsgPacket msgPacket);

    void getFile(final IOSession session, final MsgPacket msgPacket);

    void loadWebSite(final IOSession session, final MsgPacket msgPacket);

    void setWebSite(final IOSession session, final MsgPacket msgPacket);

    void httpMethod(final IOSession session, final MsgPacket msgPacket);

    void addComment(final IOSession session, final MsgPacket msgPacket);

    void deleteComment(final IOSession session, final MsgPacket msgPacket);

    void plugin(final IOSession session, final MsgPacket msgPacket);

}
