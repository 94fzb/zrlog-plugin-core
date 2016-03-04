package com.fzb.zrlog.plugin.api;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;

public interface IPluginService {

    void handle(IOSession session, MsgPacket msgPacket);
}
