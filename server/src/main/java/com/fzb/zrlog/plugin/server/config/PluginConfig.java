package com.fzb.zrlog.plugin.server.config;

import com.fzb.common.dao.impl.DAO;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.modle.BlogRunTime;
import com.fzb.zrlog.plugin.server.dao.WebSiteDAO;
import com.fzb.zrlog.plugin.type.RunType;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.log4j.Logger;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class PluginConfig {

    private static final String PLUGIN_DB_KEY = "plugin_core_db_key";
    private static final Logger LOGGER = Logger.getLogger(PluginConfig.class);
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
        ComboPooledDataSource dataSource = new ComboPooledDataSource(false);
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(_dbPropertiesFile));
            dataSource.setJdbcUrl(properties.get("jdbcUrl").toString() + "&autoReconnect=true");
            dataSource.setMaxIdleTime(20);
            dataSource.setAcquireIncrement(2);
            dataSource.setInitialPoolSize(10);
            dataSource.setMaxPoolSize(100);
            dataSource.setMinPoolSize(10);
            dataSource.setPassword(properties.get("password").toString());
            dataSource.setDriverClass(properties.get("driverClass").toString());
            dataSource.setUser(properties.get("user").toString());
        } catch (IOException | PropertyVetoException e) {
            e.printStackTrace();
        }
        DAO.setDs(dataSource);
        try {
            String text = (String) new WebSiteDAO().set("name", PLUGIN_DB_KEY).queryFirst("value");
            if (text != null && !"".equals(text)) {
                instance.pluginCore = new JSONDeserializer<PluginCore>().deserialize(text);
            } else {
                instance.pluginCore = new PluginCore();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        saveToJsonFileThread();
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
                        LOGGER.error("stop", e);
                    }
                    String jsonStr = new JSONSerializer().deepSerialize(getInstance().pluginCore);
                    if (!currentPluginText.equals(jsonStr)) {
                        currentPluginText = jsonStr;
                        try {
                            new WebSiteDAO().saveOrUpdate(PLUGIN_DB_KEY, currentPluginText);
                        } catch (SQLException e) {
                            LOGGER.error(e);
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
        Collection<PluginVO> pluginVOs = pluginCore.getPluginInfoMap().values();
        return pluginVOs;
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
