package com.fzb.net.socket;


import com.zrlog.plugin.IOSession;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface ISocketServer {

    void listener();

    void destroy();

    void create();

    void dispose(IOSession session, SocketChannel channel, SelectionKey selectionKey);
}
