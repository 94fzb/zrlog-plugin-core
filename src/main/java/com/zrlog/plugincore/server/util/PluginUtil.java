package com.zrlog.plugincore.server.util;

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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/11.
 */
public class PluginUtil {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);

    private static final Map<String, File> idFileMap = new ConcurrentHashMap<>();

    private static final ReentrantLock reentrantLock = new ReentrantLock();

    private static final Map<String, Process> processMap = new ConcurrentHashMap<>();

    public static void loadJarPlugin() {
        try {
            registerHook();
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new PluginScanRunnable(), 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "start plugin exception ", e);
        }

    }

    private static String getProgramName(File file) {
        if (file.getName().endsWith(".jar")) {
            if (Objects.isNull(System.getProperty("java.home"))) {
                return "java";
            }
            return System.getProperty("java.home") + "/bin/java";
        }
        return file.toString();
    }

    public static void loadPlugin(final File file, String pluginId) {
        if (file == null || !file.exists()) {
            return;
        }
        String pluginName = file.getName()
                .replace("-Darwin", "")
                .replace("-x86_64", "")
                .replace("-Linux", "")
                .replace("-arm64", "")
                .replace(".jar", "");
        if (processMap.containsKey(pluginName)) {
            return;
        }
        LOGGER.info("run plugin " + pluginName);
        reentrantLock.lock();
        try {
            idFileMap.put(pluginId, file);
            String userDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/usr/";
            String tmpDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/tmp/";
            new File(userDir).mkdirs();
            new File(tmpDir).mkdirs();
            Process pr = CmdUtil.getProcess(getProgramName(file),
                    "-Djava.io.tmpdir=" + tmpDir, "-Duser.dir=" + userDir, ConfigKit.get("pluginJvmArgs", ""), "-jar "
                            + file + " " + PluginConfig.getInstance().getMasterPort() + " " + pluginId);
            if (pr != null) {
                processMap.put(pluginId, pr);
                printInputStreamWithThread(pr, pr.getInputStream(), pluginName, "PINFO", pluginId);
                printInputStreamWithThread(pr, pr.getErrorStream(), pluginName, "PERROR", pluginId);
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    public static void registerPlugin(PluginStatus pluginStatus, IOSession session) {
        if (Objects.isNull(session)) {
            return;
        }
        if (pluginStatus != PluginStatus.START && pluginStatus != PluginStatus.WAIT_INSTALL) {
            throw new IllegalArgumentException("status must be not " + pluginStatus);
        }
        PluginVO pluginVO = new PluginVO();
        if (RunConstants.runType == RunType.BLOG) {
            pluginVO.setFile(idFileMap.get(session.getPlugin().getId()).toString());
        }
        pluginVO.setStatus(pluginStatus);
        pluginVO.setPlugin(session.getPlugin());
        PluginConfig.getInstance().getPluginInfoMap().put(session.getPlugin().getShortName(), pluginVO);
        PluginConfig.getInstance().getSessionMap().put(session.getPlugin().getId(), session);
    }

    public static void stopPlugin(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        String pluginId = pluginVO.getPlugin().getId();
        IOSession session = PluginConfig.getInstance().getSessionMap().get(pluginId);
        session.close();

        destroy(pluginName);

        PluginConfig.getInstance().getPluginVOByName(pluginName).setStatus(PluginStatus.STOP);
    }

    public static void deletePlugin(String pluginName) {
        PluginVO pluginVO = PluginConfig.getInstance().getPluginVOByName(pluginName);
        if (pluginVO != null) {
            String pluginId = pluginVO.getPlugin().getId();
            IOSession session = PluginConfig.getInstance().getSessionMap().get(pluginId);
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
            String pluginId = pluginVO.getPlugin().getId();
            IOSession session = PluginConfig.getInstance().getSessionMap().get(pluginId);
            if (session != null) {
                session.close();
            }
            //关闭进程
            if (RunConstants.runType != RunType.DEV) {
                Process process = processMap.get(pluginId);
                if (process != null) {
                    process.destroy();
                }
                processMap.remove(pluginId);
            }
            //移除相关映射
            PluginConfig.getInstance().getSessionMap().remove(pluginId);
            idFileMap.remove(pluginId);
        }
    }

    private static void printInputStreamWithThread(final Process pr, final InputStream in, final String pluginName,
                                                   final String printLevel, final String uuid) {
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in));) {
                String str = br.readLine();
                if (Objects.isNull(str)) {
                    return;
                }
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
                    destroy(pluginName);
                } finally {
                    pr.destroy();
                }
            }
        }).start();
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

    static void copyInputStreamToFile(InputStream inputStream, String filePath) {
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            LOGGER.info("copy plugin error " + e.getMessage());
        } finally {
            if (Objects.nonNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }

        }
    }

    public static File downloadPluginByUrl(String url, String fileName) throws Exception {
        LOGGER.info("download plugin " + fileName);
        File downloadFile = new File(PluginConfig.getInstance().getPluginBasePath() + "/" + fileName);
        copyInputStreamToFile(HttpUtils.doGetRequest(url, new HashMap<>()), downloadFile.toString());
        if (downloadFile.length() == 0) {
            throw new RuntimeException("Download error");
        }
        return downloadFile;
    }

    public static File downloadPlugin(String fileName) throws Exception {
        String downloadUrl = "https://dl.zrlog.com/plugin/" + fileName;
        return downloadPluginByUrl(downloadUrl, fileName);

    }

    public static void main(String[] args) throws Exception {
        File file = downloadPlugin("oss.jar");
        System.out.println(file);
    }

    public static boolean isRunningByPluginId(String pluginId) {
        return processMap.containsKey(pluginId);
    }
}
