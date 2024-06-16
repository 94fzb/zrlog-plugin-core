package com.zrlog.plugincore.server.util;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.config.PluginVO;
import com.zrlog.plugincore.server.type.PluginStatus;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginScanRunnable implements Runnable {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginUtil.class);

    @Override
    public void run() {
        if (RunConstants.runType == RunType.DEV) {
            return;
        }
        checkLostFile();
        Set<PluginVO> runningPlugins = PluginConfig.getInstance().getAllPluginVO().stream().filter((pluginVO) -> {
            //插件为开启状态，且还没有启动的情况
            String pluginId = pluginVO.getPlugin().getId();
            if (Objects.isNull(pluginId)) {
                return false;
            }
            return !PluginUtil.isRunningByPluginId(pluginId);
        }).collect(Collectors.toSet());
        for (PluginVO pluginVO : runningPlugins) {
            File file = new File(pluginVO.getFile());
            if (!file.getName().endsWith(".jar") && !file.getName().endsWith(".bin") && !file.getName().endsWith(".exe")) {
                continue;
            }
            String pluginId = pluginVO.getPlugin().getId();
            PluginUtil.loadPlugin(file, pluginId);
        }
    }

    private void checkLostFile() {
        boolean download = !PluginConfig.getInstance().getPluginCore().getSetting().isDisableAutoDownloadLostFile();
        if (!download) {
            return;
        }
        for (PluginVO pluginVO : PluginConfig.getInstance().getAllPluginVO()) {
            if (pluginVO.getStatus() != PluginStatus.START) {
                continue;
            }
            if (Objects.nonNull(pluginVO.getFile())) {
                File file = new File(pluginVO.getFile());
                if (file.exists() && file.length() > 0) {
                    continue;
                }
            }
            try {
                File downloadFile = PluginUtil.downloadPlugin(PluginUtil.getPluginFile(pluginVO.getPlugin().getShortName()));
                pluginVO.setFile(downloadFile.toString());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "download error", e);
            }
        }
    }
}
