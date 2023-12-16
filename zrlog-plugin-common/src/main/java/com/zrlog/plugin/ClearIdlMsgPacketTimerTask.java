package com.zrlog.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

public class ClearIdlMsgPacketTimerTask extends TimerTask {

    private final Map<Integer, PipeInfo> pipeMap;

    public ClearIdlMsgPacketTimerTask(Map<Integer, PipeInfo> pipeMap) {
        this.pipeMap = pipeMap;
    }

    @Override
    public void run() {
        Set<Integer> integerSet = new HashSet<>();
        for (Map.Entry<Integer, PipeInfo> entry : pipeMap.entrySet()) {
            long activeTime = System.currentTimeMillis() - entry.getValue().getCratedAt();
            if (activeTime > 60000 * 10) {
                integerSet.add(entry.getKey());
            }
        }
        for (Integer i : integerSet) {
            pipeMap.remove(i);
        }
    }
}
