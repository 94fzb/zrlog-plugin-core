package com.fzb.zrlog.plugin.server;

import com.fzb.common.util.CmdUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/11.
 */
public class PluginConfig {

    private static Logger LOGGER = Logger.getLogger(PluginConfig.class);
    private static Map<String, Process> processMap = new HashMap<>();

    public static void loadJarPlugin(File pluginBasePath, final int serverPort) {
        try {
            registerHook(processMap);
            File[] files = pluginBasePath.listFiles();
            if (files != null && files.length > 0) {
                for (final File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        new Thread() {
                            @Override
                            public void run() {
                                if (DataMap.getFilePluginStatusMap().get(file.toString()) != PluginStatus.STOP) {
                                    startPlugin(file, serverPort);
                                }
                            }


                        }.start();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("start plugin exception ", e);
        }
    }

    public static void startPlugin(File file, int serverPort) {
        Process pr = CmdUtil.getProcess("java -Xms4m -Xmx16m -jar " + file.toString() + " " + serverPort + " " + file.toString());
        String pluginName = file.getName().replace(".jar", "");
        if (pr != null) {
            processMap.put(pluginName, pr);
            printInputStreamWithThread(pr.getInputStream(), pluginName);
            printInputStreamWithThread(pr.getErrorStream(), pluginName);
        }
        try {
            // 等待链接初始化完成
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void printInputStreamWithThread(final InputStream in, final String pluginName) {
        new Thread() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str;
                try {
                    while ((str = br.readLine()) != null) {
                        System.out.println("PPP: " + pluginName + "  - " + str);
                    }
                } catch (IOException e) {
                    LOGGER.error("plugin output error", e);
                }
            }
        }.start();
    }

    private static void registerHook(final Map<String, Process> processMap) {
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Map.Entry<String, Process> entry : processMap.entrySet()) {
                    entry.getValue().destroyForcibly();
                    LOGGER.info("close plugin " + " " + entry.getKey());
                }
            }
        });
    }
}
