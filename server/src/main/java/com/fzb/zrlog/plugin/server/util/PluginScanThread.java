package com.fzb.zrlog.plugin.server.util;

import com.fzb.common.util.RunConstants;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.config.PluginVO;
import com.fzb.zrlog.plugin.server.type.PluginStatus;
import com.fzb.zrlog.plugin.type.RunType;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

public class PluginScanThread extends TimerTask {

    private static Logger LOGGER = Logger.getLogger(PluginUtil.class);

    @Override
    public void run() {
        if (RunConstants.runType == RunType.BLOG) {
            File[] files = new File(PluginConfig.getInstance().getPluginBasePath()).listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        tryLoadPlugin(file);
                    }
                }
            }
        }
        checkLostFile();
    }

    private void tryLoadPlugin(File file) {
        boolean include = false;
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO.getFile() != null) {
                File tFile = new File(pluginVO.getFile());
                if (tFile.exists()) {
                    if (pluginVO.getFile().equals(file.toString())) {
                        include = true;
                        if (!PluginUtil.processMap.containsKey(pluginVO.getSessionId()) && pluginVO.getStatus() == PluginStatus.START) {
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
                if (!file.exists()) {
                    boolean download = PluginConfig.getInstance().getPluginCore().getSetting().isAutoDownloadLostFile();
                    if (download) {
                        try {
                            pluginVO.setFile(PluginUtil.downloadPlugin(file.getName(), "http://dl.zrlog.com/plugin/" + file.getName()).toString());
                        } catch (IOException e) {
                            LOGGER.error("download error" + file.getName(), e);
                        }
                    }
                }
            }
        }
    }
}
