package com.fzb.zrlog.plugin.api;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;

public interface IConnectHandler {

    void handler(final IOSession session, final MsgPacket msgPacket);
}
