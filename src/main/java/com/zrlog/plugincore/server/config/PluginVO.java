package com.zrlog.plugincore.server.config;

import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugincore.server.type.PluginStatus;

public class PluginVO {

    private Plugin plugin;
    private PluginStatus status;
    private String file;

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public PluginStatus getStatus() {
        return status;
    }

    public void setStatus(PluginStatus status) {
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
