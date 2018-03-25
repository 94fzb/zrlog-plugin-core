package com.fzb.zrlog.plugin;

import com.fzb.zrlog.plugin.common.LoggerUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClearIdlMsgPacket extends Thread {

    private static Logger LOGGER = LoggerUtil.getLogger(ClearIdlMsgPacket.class);

    private Map<Integer, Object[]> pipeMap;

    public ClearIdlMsgPacket(Map<Integer, Object[]> pipeMap) {
        this.pipeMap = pipeMap;
    }

    @Override
    public void run() {
        while (true) {
            Set<Integer> integerSet = new HashSet<>();
            for (Map.Entry<Integer, Object[]> entry : pipeMap.entrySet()) {
                if (entry.getValue().length > 5) {
                    long activeTime = System.currentTimeMillis() - (long) entry.getValue()[5];
                    if (activeTime > 10000) {
                        integerSet.add(entry.getKey());
                    }
                }
            }
            for (Integer i : integerSet) {
                pipeMap.remove(i);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        }
    }
}
