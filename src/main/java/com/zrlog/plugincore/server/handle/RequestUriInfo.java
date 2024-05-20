package com.zrlog.plugincore.server.handle;

public class RequestUriInfo {

    private String pluginName;
    private String action;


    public RequestUriInfo(String pluginName, String action) {
        this.pluginName = pluginName;
        this.action = action;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
