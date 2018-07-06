package com.zrlog.plugin.api;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.data.codec.MsgPacket;

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

    void getDbProperties(final IOSession session, final MsgPacket msgPacket);

    void attachment(final IOSession session, final MsgPacket msgPacket);

    void loadPublicInfo(final IOSession session, final MsgPacket msgPacket);

    void getCurrentTemplate(final IOSession session, final MsgPacket msgPacket);

    void getBlogRuntimePath(final IOSession session, final MsgPacket msgPacket);

    void createArticle(final IOSession session, final MsgPacket msgPacket);
}
