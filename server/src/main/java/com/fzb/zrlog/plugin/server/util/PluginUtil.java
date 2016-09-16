package com.fzb.zrlog.plugin.server.util;

import com.fzb.common.util.CmdUtil;
import com.fzb.common.util.IOUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.common.util.http.HttpUtil;
import com.fzb.common.util.http.handle.HttpFileHandle;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.ConfigKit;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.config.PluginVO;
import com.fzb.zrlog.plugin.server.type.PluginStatus;
import com.fzb.zrlog.plugin.type.RunType;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by xiaochun on 2016/2/11.
 */
public class PluginUtil {

    private static Logger LOGGER = Logger.getLogger(PluginUtil.class);
    protected static Map<String, Process> processMap = new HashMap<>();
    private static Map<String, File> idFileMap = new HashMap<>();

    public static void loadJarPlugin() {
        try {
            registerHook(processMap);
            Timer timer = new Timer();
            timer.schedule(new PluginScanThread(), 0, 5000);
        } catch (Exception e) {
            LOGGER.warn("start plugin exception ", e);
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
                        Process pr = CmdUtil.getProcess("java " + ConfigKit.get("pluginJvmArgs", "") + " -jar " + file.toString() + " " + PluginConfig.getInstance().getMasterPort() + " " + uuid);
                        if (pr != null) {
                            processMap.put(uuid, pr);
                            printInputStreamWithThread(pr.getInputStream(), pluginName, "PINFO", uuid);
                            printInputStreamWithThread(pr.getErrorStream(), pluginName, "PERROR", uuid);
                        }
                    }
                }
            }
        }.start();
        try {
            // 等待链接初始化完成
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        String sessionId = pluginVO.getSessionId();
        IOSession session = PluginConfig.getInstance().getSessionMap().get(sessionId);
        if (session != null) {
            session.close();
            destroy(pluginName);
        }

        if (RunConstants.runType != RunType.DEV) {
            new File(pluginVO.getFile()).delete();
        }
        PluginConfig.getInstance().getPluginInfoMap().remove(pluginName);
    }

    private static void destroy(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        String sessionId = pluginVO.getSessionId();
        //关闭进程
        if (RunConstants.runType != RunType.DEV) {
            Process process = processMap.get(sessionId);
            process.destroy();
            processMap.remove(sessionId);
        }
        //移除相关映射
        PluginConfig.getInstance().getSessionMap().remove(sessionId);
        idFileMap.remove(sessionId);
    }

    private static void printInputStreamWithThread(final InputStream in, final String pluginName, final String pr, final String uuid) {
        new Thread() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str;
                try {
                    str = br.readLine();
                    if ("PERROR".equals(pr) && str.startsWith("Error: Invalid or corrupt jarfile")) {
                        processMap.remove(uuid);
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

    public static void downloadPlugin(String fileName, String downloadUrl) throws IOException {
        LOGGER.info("download plugin " + fileName);
        HttpFileHandle fileHandle = (HttpFileHandle) HttpUtil.sendGetRequest(downloadUrl, new HttpFileHandle(PluginConfig.getInstance().getPluginBasePath()), new HashMap<String, String>());
        String target = fileHandle.getT().getParent() + "/" + fileName;
        if (!target.equals(fileHandle.getT().toString())) {
            IOUtil.moveOrCopyFile(fileHandle.getT().toString(), target, true);
        }
    }
}