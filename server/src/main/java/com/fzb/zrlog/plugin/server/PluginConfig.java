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

    public static void loadJarPlugin(File pluginBasePath, final int serverPort) {
        try {
            final Map<String, Process> processMap = new HashMap<String, Process>();
            registerHook(processMap);
            File[] files = pluginBasePath.listFiles();
            if (files != null && files.length > 0) {
                for (final File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        new Thread() {
                            @Override
                            public void run() {
                                Process pr = CmdUtil.getProcess("java -Xms4m -Xmx16m -jar " + file.toString() + " " + serverPort);
                                String pluginName = file.getName().replace(".jar", "");
                                if (pr != null) {
                                    processMap.put(pluginName, pr);
                                    printInputStreamWithThread(pr.getInputStream(), pluginName);
                                    printInputStreamWithThread(pr.getErrorStream(), pluginName);
                                }
                            }

                            private void printInputStreamWithThread(final InputStream in, final String pluginName) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                        String str;
                                        try {
                                            while ((str = br.readLine()) != null) {
                                                System.out.println("plugin " + pluginName + "  - " + str);
                                            }
                                        } catch (IOException e) {
                                            LOGGER.error("plugin output error", e);
                                        }
                                    }
                                }.start();
                            }
                        }.start();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("start plugin exception ", e);
        }
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
