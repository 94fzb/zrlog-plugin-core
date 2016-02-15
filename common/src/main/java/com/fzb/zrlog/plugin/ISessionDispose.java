package com.fzb.zrlog.plugin;

import com.fzb.zrlog.plugin.data.codec.MsgPacket;

/**
 * Created by xiaochun on 2016/2/12.
 */
public interface ISessionDispose {

    void handler(IOSession session, MsgPacket packet);
}
