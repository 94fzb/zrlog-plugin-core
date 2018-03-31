package com.zrlog.plugincore.server.util;

import com.fzb.common.util.RunConstants;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;
import com.fzb.zrlog.plugin.type.RunType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginScanThread extends TimerTask {

    private static Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);

    @Override
    public void run() {
        checkLostFile();
        if (RunConstants.runType == RunType.BLOG) {
            List<String> fileList = new ArrayList<>();
            fillPluginFileByExistsPlugins(fileList);

            fillPluginFileByBasePath(fileList);
            for (String file : fileList) {
                if (file != null) {
                    if (new File(file).getName().endsWith(".jar")) {
                        tryLoadPlugin(new File(file));
                    }
                }
            }
        }

    }

    private void fillPluginFileByExistsPlugins(List<String> fileList) {
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO != null) {
                fileList.add(pluginVO.getFile());
            }
        }
    }

    private void fillPluginFileByBasePath(List<String> fileList) {
        File[] files = new File(PluginConfig.getInstance().getPluginBasePath()).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!fileList.contains(file.toString())) {
                    fileList.add(file.toString());
                }
            }
        }
    }

    private void tryLoadPlugin(File file) {
        boolean include = false;
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO.getFile() != null) {
                File tFile = new File(pluginVO.getFile());
                if (tFile.exists()) {
                    if (pluginVO.getFile().equals(file.toString())) {
                        include = true;
                        if (pluginVO.getSessionId() != null && !PluginUtil.processMap.containsKey(pluginVO.getSessionId()) && pluginVO.getStatus() == PluginStatus.START) {
                            PluginUtil.loadPlugin(file);
                            break;
                        }
                    }
                }
            }
        }
        if (!include) {
            PluginUtil.loadPlugin(file);
        }
    }

    private void checkLostFile() {
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO.getFile() != null) {
                File file = new File(pluginVO.getFile());
                if (!file.exists() || file.length() == 0) {
                    boolean download = !PluginConfig.getInstance().getPluginCore().getSetting().isDisableAutoDownloadLostFile();
                    String fileName = pluginVO.getPlugin().getShortName() + ".jar";
                    if (download && RunConstants.runType == RunType.BLOG) {
                        try {
                            pluginVO.setFile(PluginUtil.downloadPlugin(fileName, "http://dl.zrlog.com/plugin/" + fileName).toString());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "download error", e);
                        }
                    }
                }
            }
        }
    }
}