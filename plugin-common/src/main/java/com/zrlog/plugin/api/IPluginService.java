package com.zrlog.plugin.api;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.data.codec.MsgPacket;

public interface IPluginService {

    void handle(IOSession session, MsgPacket msgPacket);
}
