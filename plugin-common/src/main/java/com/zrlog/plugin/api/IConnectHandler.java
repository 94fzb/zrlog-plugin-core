package com.zrlog.plugin.api;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.data.codec.MsgPacket;

public interface IConnectHandler {

    void handler(final IOSession session, final MsgPacket msgPacket);
}
