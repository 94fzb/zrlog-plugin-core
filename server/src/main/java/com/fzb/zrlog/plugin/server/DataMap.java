package com.fzb.zrlog.plugin.server;

import com.fzb.common.util.IOUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.type.RunType;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
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
    private static Map<String, PluginStatus> pluginStatusMap = new ConcurrentHashMap<>();
    private static File dbProperties;
    private static File file = new File(PathKit.getRootPath() + "/plugin.txt");
    private static Map<String, PluginStatus> filePluginStatusMap = new ConcurrentHashMap<>();
    private static Map<String, File> pluginFileMap = new ConcurrentHashMap<>();
    private static String SPLIT_FLAG = "!!!!";

    public static void initData(String dbProperties) {
        if (file.exists()) {
            List<String> plugins;
            try {
		plugins = Arrays.asList(IOUtil.getStringInputStream(new FileInputStream(file)).split("\n")); 
                for (String plugin : plugins) {
                    String pluginArr[] = plugin.split(SPLIT_FLAG);
                    Plugin p = new JSONDeserializer<Plugin>().deserialize(pluginArr[0]);
                    DataMap.getPluginInfoMap().put(p.getShortName(), p);
                    pluginStatusMap.put(p.getShortName(), PluginStatus.valueOf(pluginArr[1]));
                    filePluginStatusMap.put(pluginArr[2], PluginStatus.valueOf(pluginArr[1]));
                    pluginFileMap.put(p.getShortName(), new File(pluginArr[2]));
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        DataMap.dbProperties = new File(dbProperties);
        saveToJsonFileThread();
    }

    private static void saveToJsonFileThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map.Entry<String, Plugin> entry : pluginInfoMap.entrySet()) {
                        PluginStatus status = pluginStatusMap.get(entry.getKey()) == null ? PluginStatus.STOP : pluginStatusMap.get(entry.getKey());
                        stringBuilder.append(new JSONSerializer().deepSerialize(entry.getValue()))
                                .append(SPLIT_FLAG).append(status.name())
                                .append(SPLIT_FLAG).append(entry.getValue().getId())
                                .append("\n");
                    }
                    file.delete();
                    IOUtil.writeBytesToFile(stringBuilder.toString().getBytes(), file);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
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

    public static Map<String, PluginStatus> getPluginStatusMap() {
        return pluginStatusMap;
    }

    public static File getDbProperties() {
        return dbProperties;
    }

    public static Map<String, PluginStatus> getFilePluginStatusMap() {
        return filePluginStatusMap;
    }

    public static Map<String, File> getPluginFileMap() {
        return pluginFileMap;
    }
}
