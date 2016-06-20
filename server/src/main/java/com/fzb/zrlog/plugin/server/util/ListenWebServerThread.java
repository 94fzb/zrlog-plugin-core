package com.fzb.zrlog.plugin.server.util;

import com.fzb.http.kit.IOUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenWebServerThread extends Thread {

    private static Logger LOGGER = Logger.getLogger(ListenWebServerThread.class);

    private int port;

    public ListenWebServerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            // padding await the web server exception shutdown
            IOUtil.getByteByInputStream(inputStream);
            socket.close();
            serverSocket.close();
            System.exit(0);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
