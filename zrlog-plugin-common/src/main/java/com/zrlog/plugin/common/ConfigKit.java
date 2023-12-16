package com.zrlog.plugin.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigKit {

    private static final Logger LOGGER = LoggerUtil.getLogger(ConfigKit.class);

    private static final Properties prop = new Properties();

    static {
        try {
            InputStream in = ConfigKit.class.getResourceAsStream("/conf.properties");
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    public static Integer getServerPort() {
        Object port = prop.get("server.port");
        if (port != null) {
            return Integer.parseInt(port.toString());
        }
        return 9090;
    }

    public static Object get(String key, Object defaultValue) {
        Object obj = prop.get(key);
        if (obj != null) {
            return obj;
        }
        return defaultValue;
    }
}
