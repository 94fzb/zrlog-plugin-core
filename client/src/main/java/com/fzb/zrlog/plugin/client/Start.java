package com.fzb.zrlog.plugin.client;


import com.fzb.zrlog.plugin.client.controller.DuoShuoContrlloer;
import com.fzb.zrlog.plugin.common.ConfigKit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Start {
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", ConfigKit.getServerPort());
        List<Class> classList = new ArrayList<>();
        classList.add(DuoShuoContrlloer.class);
        new NioClient().connectServerByProperties(serverAddress, classList, "/plugin.properties");
    }
}

