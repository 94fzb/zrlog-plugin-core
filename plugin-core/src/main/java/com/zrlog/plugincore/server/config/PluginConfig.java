package com.zrlog.plugincore.server.config;

import com.fzb.common.dao.impl.DAO;
import com.google.gson.Gson;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.modle.BlogRunTime;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.dao.WebSiteDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginConfig {

    private static final String PLUGIN_DB_KEY = "plugin_core_db_key";
    private static final Logger LOGGER = LoggerUtil.getLogger(PluginConfig.class);
    private static PluginConfig instance = new PluginConfig();
    private File dbPropertiesFile;
    private RunType runType;
    private int masterPort;
    private String pluginBasePath;
    private Map<String, IOSession> sessionMap = new HashMap<>();
    private PluginCore pluginCore;
    private BlogRunTime blogRunTime;

    private PluginConfig() {
    }

    public static PluginConfig getInstance() {
        return instance;
    }

    public static void init(RunType _runType, File _dbPropertiesFile, int masterPort, String pluginBasePath, BlogRunTime blogRunTime) {
        instance.runType = _runType;
        instance.dbPropertiesFile = _dbPropertiesFile;
        instance.masterPort = masterPort;
        instance.pluginBasePath = pluginBasePath;
        instance.blogRunTime = blogRunTime;
        new File(pluginBasePath).mkdir();
        MysqlDataSource dataSource = new MysqlDataSource();
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(_dbPropertiesFile));
            dataSource.setUrl(properties.get("jdbcUrl").toString() + "&autoReconnect=true");
            dataSource.setPassword(properties.get("password").toString());
            dataSource.setUser(properties.get("user").toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
        DAO.setDs(dataSource);
        try {
            String text = (String) new WebSiteDAO().set("name", PLUGIN_DB_KEY).queryFirst("value");
            if (text != null && !"".equals(text)) {
                instance.pluginCore = new Gson().fromJson(text, PluginCore.class);
            } else {
                instance.pluginCore = new PluginCore();
            }
            saveToJsonFileThread();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private static void saveToJsonFileThread() {
        new Thread() {
            @Override
            public void run() {
                String currentPluginText = "";
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "stop", e);
                    }
                    if (getInstance().pluginCore != null) {
                        String jsonStr = new Gson().toJson(getInstance().pluginCore);
                        if (!currentPluginText.equals(jsonStr)) {
                            currentPluginText = jsonStr;
                            try {
                                new WebSiteDAO().saveOrUpdate(PLUGIN_DB_KEY, currentPluginText);
                            } catch (SQLException e) {
                                LOGGER.log(Level.SEVERE, "", e);
                            }
                        }
                    }
                }
            }
        }.start();
    }

    public File getDbPropertiesFile() {
        return dbPropertiesFile;
    }

    public RunType getRunType() {
        return runType;
    }

    public int getMasterPort() {
        return masterPort;
    }

    public String getPluginBasePath() {
        return pluginBasePath;
    }

    public IOSession getIOSessionByPluginName(String pluginName) {
        PluginVO pluginVO = pluginCore.getPluginInfoMap().get(pluginName);
        if (pluginVO != null) {
            return sessionMap.get(pluginVO.getSessionId());
        }
        return null;
    }

    public IOSession getIOSessionByService(String service) {
        for (PluginVO pluginVO : pluginCore.getPluginInfoMap().values()) {
            if (pluginVO.getPlugin().getServices().contains(service)) {
                return sessionMap.get(pluginVO.getSessionId());
            }
        }
        return null;
    }

    public Map<String, IOSession> getSessionMap() {
        return sessionMap;
    }

    public Collection<PluginVO> getAllPluginVO() {
        if (pluginCore != null && pluginCore.getPluginInfoMap() != null) {
            return pluginCore.getPluginInfoMap().values();
        }
        return new ArrayList<>();
    }

    public Map<String, PluginVO> getPluginInfoMap() {
        return pluginCore.getPluginInfoMap();
    }

    public PluginVO getPluginVOByName(String pluginName) {
        return pluginCore.getPluginInfoMap().get(pluginName);
    }

    public String getPluginFileByName(String pluginName) {
        PluginVO pluginVO = pluginCore.getPluginInfoMap().get(pluginName);
        if (pluginVO != null) {
            return pluginVO.getFile();
        }
        return null;
    }

    public PluginCore getPluginCore() {
        return pluginCore;
    }

    public BlogRunTime getBlogRunTime() {
        return blogRunTime;
    }
}
