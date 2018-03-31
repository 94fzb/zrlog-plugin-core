package com.zrlog.plugin;

import com.zrlog.plugin.data.codec.MsgPacket;

/**
 * Created by xiaochun on 2016/2/14.
 */
public interface IMsgPacketCallBack {
    void handler(MsgPacket responseMsgPacket);
}
