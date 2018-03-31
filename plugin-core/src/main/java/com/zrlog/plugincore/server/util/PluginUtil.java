package com.zrlog.plugincore.server.util;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.LoggerUtil;
import com.hibegin.common.util.CmdUtil;
import com.hibegin.common.util.FileUtils;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;
import com.zrlog.plugin.type.RunType;
import com.hibegin.common.util.http.HttpUtil;
import com.hibegin.common.util.http.handle.HttpFileHandle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/11.
 */
public class PluginUtil {

    private static Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);
    protected static Map<String, Process> processMap = new HashMap<>();
    private static Map<String, File> idFileMap = new HashMap<>();

    public static void loadJarPlugin() {
        try {
            registerHook(processMap);
            Timer timer = new Timer();
            timer.schedule(new PluginScanThread(), 0, 5000);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "start plugin exception ", e);
        }

    }

    public static void loadPlugin(final File file) {
        new Thread() {
            @Override
            public void run() {
                if (file != null && file.exists()) {
                    String pluginName = file.getName().replace(".jar", "");
                    if (!processMap.containsKey(pluginName)) {
                        LOGGER.info("run plugin " + pluginName);
                        String uuid = UUID.randomUUID().toString();
                        idFileMap.put(uuid, file);
                        String javaHome = System.getProperty("java.home");
                        Process pr = CmdUtil.getProcess(javaHome + "/bin/java " + ConfigKit.get("pluginJvmArgs", "") + " -jar " + file.toString() + " " + PluginConfig.getInstance().getMasterPort() + " " + uuid);
                        if (pr != null) {
                            processMap.put(uuid, pr);
                            printInputStreamWithThread(pr, pr.getInputStream(), pluginName, "PINFO", uuid);
                            printInputStreamWithThread(pr, pr.getErrorStream(), pluginName, "PERROR", uuid);
                        }
                    }
                }
            }
        }.start();
        try {
            // 等待链接初始化完成
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    public static void registerPlugin(String id, PluginStatus pluginStatus, IOSession session) {
        if (pluginStatus != PluginStatus.START && pluginStatus != PluginStatus.WAIT_INSTALL) {
            throw new IllegalArgumentException("status must be not " + pluginStatus);
        }

        if (RunConstants.runType == RunType.DEV) {
            id = UUID.randomUUID().toString();
        }
        PluginVO pluginVO = new PluginVO();
        if (RunConstants.runType == RunType.BLOG) {
            pluginVO.setFile(idFileMap.get(id).toString());
        }
        pluginVO.setStatus(pluginStatus);
        pluginVO.setPlugin(session.getPlugin());
        pluginVO.setSessionId(id);
        //先关闭以前的连接的插件
        destroy(session.getPlugin().getShortName());
        PluginConfig.getInstance().getPluginInfoMap().put(session.getPlugin().getShortName(), pluginVO);
        PluginConfig.getInstance().getSessionMap().put(id, session);
    }

    public static void stopPlugin(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        String sessionId = pluginVO.getSessionId();
        IOSession session = PluginConfig.getInstance().getSessionMap().get(sessionId);
        session.close();

        destroy(pluginName);

        PluginConfig.getInstance().getPluginVOByName(pluginName).setStatus(PluginStatus.STOP);
    }

    public static void deletePlugin(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        if (pluginVO != null) {
            String sessionId = pluginVO.getSessionId();
            IOSession session = PluginConfig.getInstance().getSessionMap().get(sessionId);
            if (session != null) {
                session.close();
                destroy(pluginName);
            }
            if (RunConstants.runType != RunType.DEV) {
                if (pluginVO.getFile() != null) {
                    new File(pluginVO.getFile()).delete();
                }
            }
        }
        PluginConfig.getInstance().getPluginInfoMap().remove(pluginName);
    }

    private static void destroy(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        if (pluginVO != null) {
            String sessionId = pluginVO.getSessionId();
            IOSession session = PluginConfig.getInstance().getSessionMap().get(sessionId);
            if (session != null) {
                session.close();
            }
            //关闭进程
            if (RunConstants.runType != RunType.DEV) {
                Process process = processMap.get(sessionId);
                if (process != null) {
                    process.destroy();
                }
                processMap.remove(sessionId);
            }
            //移除相关映射
            PluginConfig.getInstance().getSessionMap().remove(sessionId);
            idFileMap.remove(sessionId);
        }
    }

    private static void printInputStreamWithThread(final Process pr, final InputStream in, final String pluginName, final String printLevel, final String uuid) {
        new Thread() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str;
                try {
                    str = br.readLine();
                    if ("PERROR".equals(printLevel) && str.startsWith("Error: Invalid or corrupt jarfile")) {
                        processMap.remove(uuid);
                    } else {
                        while ((str = br.readLine()) != null) {
                            System.out.println("[" + printLevel + "]" + ": " + pluginName + " - " + str);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "plugin output error", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "", e);
                    }
                    pr.destroy();
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
                    entry.getValue().destroy();
                    LOGGER.info("close plugin " + " " + entry.getKey());
                }
            }
        });
    }

    public static File downloadPlugin(String fileName, String downloadUrl) throws IOException {
        LOGGER.info("download plugin " + fileName);
        String tempFolder = PluginConfig.getInstance().getPluginBasePath() + "/tmp/";
        new File(tempFolder).mkdirs();
        HttpFileHandle fileHandle = (HttpFileHandle) HttpUtil.getInstance().sendGetRequest(downloadUrl, new HttpFileHandle(tempFolder), new HashMap<String, String>());
        String target = PluginConfig.getInstance().getPluginBasePath() + "/" + fileName;
        if (!target.equals(fileHandle.getT().toString())) {
            FileUtils.moveOrCopyFile(fileHandle.getT().toString(), target, true);
        }
        return new File(target);
    }
}
