package com.zrlog.plugincore.server.util;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginScanThread extends TimerTask {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);

    @Override
    public void run() {
        if (RunConstants.runType != RunType.BLOG) {
            return;
        }
        checkLostFile();
        Set<String> fileSet =
                PluginConfig.getInstance().getAllPluginVO().stream().map(PluginVO::getFile).collect(Collectors.toSet());
        File[] files = new File(PluginConfig.getInstance().getPluginBasePath()).listFiles();
        if (files != null) {
            for (File file : files) {
                fileSet.add(file.toString());
            }
        }
        for (String filePath : fileSet) {
            if (filePath == null) {
                continue;
            }
            File file = new File(filePath);
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            Optional<PluginVO> first =
                    PluginConfig.getInstance().getAllPluginVO().stream().filter(x -> Objects.equals(x.getFile(),
                            file.toString())).findFirst();
            if (first.isEmpty()) {
                return;
            }
            PluginVO pluginVO = first.get();
            //插件为开启状态，且还没有启动的情况
            if (pluginVO.getSessionId() != null && pluginVO.getStatus() == PluginStatus.START && !PluginUtil.isRunningBySessionId(pluginVO.getSessionId())) {
                PluginUtil.loadPlugin(file);
            }
        }
    }

    private void checkLostFile() {
        boolean download = !PluginConfig.getInstance().getPluginCore().getSetting().isDisableAutoDownloadLostFile();
        if (!download) {
            return;
        }
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO.getFile() == null) {
                continue;
            }
            File file = new File(pluginVO.getFile());
            if (file.exists() && file.length() > 0) {
                continue;
            }
            try {
                String fileName = pluginVO.getPlugin().getShortName() + ".jar";
                File downloadFile = PluginUtil.downloadPlugin( fileName,
                        "https://dl.zrlog.com/plugin/" + fileName);
                pluginVO.setFile(downloadFile.toString());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "download error", e);
            }
        }
    }
}
