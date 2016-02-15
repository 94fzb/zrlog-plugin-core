package com.fzb.zrlog.plugin.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigKit {

    private static Properties prop = new Properties();

    static {
        try {
            InputStream in = ConfigKit.class.getResourceAsStream("/conf.properties");
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
