package com.fzb.zrlog.plugin.server;

import com.fzb.http.kit.PathKit;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.type.RunType;
import flexjson.JSONDeserializer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class DataMap {

    private static Logger LOGGER = Logger.getLogger(DataMap.class);

    private static Map<String, IOSession> pluginMap = new ConcurrentSkipListMap<>();
    private static Map<String, IOSession> serviceMap = new ConcurrentSkipListMap<>();
    private static Map<String, IOSession> actionMap = new ConcurrentSkipListMap<>();
    private static Map<String, Object> pathMap = new ConcurrentSkipListMap<>();
    private static Map<String, Plugin> pluginInfoMap = new ConcurrentSkipListMap<>();

    public static void initData() {
        File file = new File(PathKit.getRootPath() + "/plugin.json");
        if (file.exists()) {
            List<String> plugins = null;
            try {
                plugins = Files.readAllLines(Paths.get(file.toURI()));
            } catch (IOException e) {
                LOGGER.error(e);
            }
            for (String plugin : plugins) {
                Plugin p = new JSONDeserializer<Plugin>().deserialize(plugin);
                DataMap.getPluginInfoMap().put(p.getShortName(), p);
            }
        }
    }

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
