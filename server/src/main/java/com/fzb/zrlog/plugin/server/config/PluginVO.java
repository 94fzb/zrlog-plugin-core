package com.fzb.zrlog.plugin.server.config;

import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.server.type.PluginStatus;

import java.io.File;

public class PluginVO {

    private Plugin plugin;
    private PluginStatus status;
    private String file;
    private String sessionId;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
