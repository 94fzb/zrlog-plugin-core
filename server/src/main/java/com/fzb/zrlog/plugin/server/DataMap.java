package com.fzb.zrlog.plugin.server;

import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.message.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class DataMap {

    private static Map<String, IOSession> pluginMap = new ConcurrentHashMap<>();
    private static Map<String, IOSession> serviceMap = new ConcurrentHashMap<>();
    private static Map<String, IOSession> actionMap = new ConcurrentHashMap<>();
    private static Map<String, Object> pathMap = new ConcurrentHashMap<>();
    private static Map<String, Plugin> pluginInfoMap = new HashMap<>();

    public static Map<String, IOSession> getPluginMap() {
        return pluginMap;
    }

    public static Map<String, IOSession> getServiceMap() {
        return serviceMap;
    }

    public static Map<String, IOSession> getActionMap() {
        return actionMap;
    }

    public static Map<String, Object> getPathMap() {
        return pathMap;
    }

    public static Map<String, Plugin> getPluginInfoMap() {
        return pluginInfoMap;
    }
}
