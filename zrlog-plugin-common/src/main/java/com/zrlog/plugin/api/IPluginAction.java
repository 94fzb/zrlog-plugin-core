package com.zrlog.plugin.api;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;

public interface IPluginAction {
    //插件需要编写的代码
    void start(IOSession session, MsgPacket msgPacket);

    // 插件停止需要处理事情，比如停止
    void stop(IOSession session, MsgPacket msgPacket);

    // 插件安装(及需要读取的常量)
    void install(IOSession session, MsgPacket msgPacket, HttpRequestInfo requestInfo);

    void uninstall(IOSession session, MsgPacket msgPacket);
}