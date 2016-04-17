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

    public static void loadJarPlugin(final File pluginBasePath, final int serverPort) {
        try {
            registerHook(processMap);
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        File[] files = pluginBasePath.listFiles();
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                if (file.getName().endsWith(".jar")) {
                                    startPlugin(file, serverPort);
                                }
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            // 等待链接初始化完成
            Thread.sleep(1000);
        } catch (Exception e) {
            LOGGER.warn("start plugin exception ", e);
        }
    }

    public static void startPlugin(final File file, final int serverPort) {
        new Thread() {
            @Override
            public void run() {
                if (DataMap.getFilePluginStatusMap().get(file.toString()) == null) {
                    go(file, serverPort);
                }
                if (DataMap.getFilePluginStatusMap().get(file.toString()) != PluginStatus.STOP) {
                    go(file, serverPort);
                }
            }

            public void go(File file, int serverPort) {
                String pluginName = file.getName().replace(".jar", "");
                if (!processMap.containsKey(pluginName)) {
                    LOGGER.info("run plugin " + pluginName);
                    Process pr = CmdUtil.getProcess("java -Xms4m -Xmx16m -jar " + file.toString() + " " + serverPort + " " + file.toString());
                    if (pr != null) {
                        processMap.put(pluginName, pr);
                        printInputStreamWithThread(pr.getInputStream(), pluginName, "PINFO");
                        printInputStreamWithThread(pr.getErrorStream(), pluginName, "PERROR");
                    }
                }
            }
        }.start();

    }


    private static void printInputStreamWithThread(final InputStream in, final String pluginName, final String pr) {
        new Thread() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str;
                try {
                    str = br.readLine();
                    if ("PERROR".equals(pr) && str.startsWith("Error: Invalid or corrupt jarfile")) {
                        processMap.remove(pluginName);
                    } else {
                        while ((str = br.readLine()) != null) {
                            System.out.println("[" + pr + "]" + ": " + pluginName + " - " + str);
                        }
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
