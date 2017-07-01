package com.fzb.zrlog.plugin.timer;


import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.common.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatTimer {

    private static final Logger LOGGER = LoggerUtil.getLogger(HeartbeatTimer.class);

    public static void start(IOSession session) {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Long.parseLong(ConfigKit.get("heartbeat", 10000) + ""));
                        Map<String, Object> map = new HashMap<>();
                        map.put("status", 0);
                        //session.sendJsonMsg(map, "UserServiceRequestImpl_heartbeat");
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "heartTimer error will stop", e);
                        break;
                    }
                }
            }
        }.start();

    }
}
