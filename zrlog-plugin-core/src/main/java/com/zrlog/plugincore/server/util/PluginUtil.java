package com.zrlog.plugincore.server.util;

import com.hibegin.common.util.CmdUtil;
import com.hibegin.common.util.FileUtils;
import com.hibegin.common.util.http.HttpUtil;
import com.hibegin.common.util.http.handle.HttpFileHandle;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/11.
 */
public class PluginUtil {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);

    private static final Map<String, File> idFileMap = new HashMap<>();

    private static final ReentrantLock reentrantLock = new ReentrantLock();

    private static final Map<String, Process> processMap = new HashMap<>();

    public static void loadJarPlugin() {
        try {
            registerHook();
            new Timer().scheduleAtFixedRate(new PluginScanThread(), 0, 1000 * 120);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "start plugin exception ", e);
        }

    }

    public static void loadPlugin(final File file) {
        if (file == null || !file.exists()) {
            return;
        }
        String pluginName = file.getName().replace(".jar", "");
        if (processMap.containsKey(pluginName)) {
            return;
        }
        LOGGER.info("run plugin " + pluginName);
        reentrantLock.lock();
        try {
            String uuid = UUID.randomUUID().toString();
            idFileMap.put(uuid, file);
            String userDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/usr/";
            String tmpDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/tmp/";
            new File(userDir).mkdirs();
            new File(tmpDir).mkdirs();
            Process pr = CmdUtil.getProcess(System.getProperty("java.home") + "/bin/java",
                    "-Djava.io.tmpdir=" + tmpDir, "-Duser.dir=" + userDir, ConfigKit.get("pluginJvmArgs", ""), "-jar "
                            + file + " " + PluginConfig.getInstance().getMasterPort() + " " + uuid);
            if (pr != null) {
                processMap.put(uuid, pr);
                printInputStreamWithThread(pr, pr.getInputStream(), pluginName, "PINFO", uuid);
                printInputStreamWithThread(pr, pr.getErrorStream(), pluginName, "PERROR", uuid);
            }
        } finally {
            reentrantLock.unlock();
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

    private static void printInputStreamWithThread(final Process pr, final InputStream in, final String pluginName,
                                                   final String printLevel, final String uuid) {
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

    private static void registerHook() {
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(new Thread(() -> {
            for (Map.Entry<String, Process> entry : PluginUtil.processMap.entrySet()) {
                entry.getValue().destroy();
                LOGGER.info("close plugin " + " " + entry.getKey());
            }
        }));
    }

    public static File downloadPlugin(String fileName, String downloadUrl) throws IOException {
        LOGGER.info("download plugin " + fileName);
        String tempFolder = PluginConfig.getInstance().getPluginBasePath() + "/tmp/";
        new File(tempFolder).mkdirs();
        HttpFileHandle fileHandle = (HttpFileHandle) HttpUtil.getInstance().sendGetRequest(downloadUrl,
                new HttpFileHandle(tempFolder), new HashMap<>());
        String fileNameTarget = PluginConfig.getInstance().getPluginBasePath() + "/" + fileName;
        if (!fileNameTarget.equals(fileHandle.getT().toString())) {
            FileUtils.moveOrCopyFile(fileHandle.getT().toString(), fileNameTarget, true);
        }
        File downloadFile = new File(fileNameTarget);
        if (downloadFile.length() == 0) {
            throw new RuntimeException("Download error");
        }
        return downloadFile;
    }

    public static boolean isRunningBySessionId(String sessionId) {
        return processMap.containsKey(sessionId);
    }
}