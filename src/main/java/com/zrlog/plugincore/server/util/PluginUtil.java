package com.zrlog.plugincore.server.util;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.ConfigKit;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.Application;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;

import java.io.*;
import java.util.*;
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

    public static void loadPlugins() {
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

    public static String getPluginName(File file) {
        return file.getName()
                .replace("-Darwin", "")
                .replace("-x86_64", "")
                .replace("-Linux", "")
                .replace("-Windows", "")
                .replace("-arm64", "")
                .replace(".bin", "")
                .replace(".exe", "")
                .replace(".jar", "");
    }


    public static void loadPlugin(final File pluginFile, String pluginId) {
        if (pluginFile == null || !pluginFile.exists()) {
            return;
        }
        String pluginName = getPluginName(pluginFile);
        if (processMap.containsKey(pluginName)) {
            return;
        }
        LOGGER.info("run plugin " + pluginName);
        reentrantLock.lock();
        try {
            idFileMap.put(pluginId, pluginFile);
            String userDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/usr/";
            String tmpDir = PluginConfig.getInstance().getPluginBasePath() + "/" + pluginName + "/tmp/";
            new File(userDir).mkdirs();
            new File(tmpDir).mkdirs();
            List<String> args = new ArrayList<>();
            if (pluginFile.getName().endsWith(".jar")) {
                args.add("-Djava.io.tmpdir=" + tmpDir);
                args.add("-Duser.dir=" + userDir);
                args.add(ConfigKit.get("pluginJvmArgs", "") + "");
                args.add("-jar");
                args.add(pluginFile.toString());
                args.add(PluginConfig.getInstance().getMasterPort() + "");
                args.add(pluginId);
            } else {
                if (File.separatorChar == '/') {
                    CmdUtil.sendCmd("chmod", "a+x", pluginFile.toString());
                }
                args.add(PluginConfig.getInstance().getMasterPort() + "");
                args.add(pluginId);
                args.add("-Djava.io.tmpdir=" + tmpDir);
                args.add("-Duser.dir=" + userDir);
            }
            Process pr = CmdUtil.getProcess(getProgramName(pluginFile), args.toArray(new Object[0]));
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
                File pluginFile = getPluginFile(pluginName);
                if (pluginFile.exists()) {
                    pluginFile.delete();
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
                if (RunConstants.runType == RunType.DEV) {
                    LOGGER.log(Level.SEVERE, "plugin output error", e);
                }
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

    public static File getPluginFile(String pluginName) {
        String filename = StringUtils.isEmpty(Application.NATIVE_INFO) ? pluginName + ".jar" :
                pluginName + "-" + Application.NATIVE_INFO + (Application.NATIVE_INFO.contains("Window") ? ".exe" : ".bin");
        return new File(PluginConfig.getInstance().getPluginBasePath() + "/" + filename);
    }

    private static File downloadPluginByUrl(String url, String fileName) throws Exception {
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
