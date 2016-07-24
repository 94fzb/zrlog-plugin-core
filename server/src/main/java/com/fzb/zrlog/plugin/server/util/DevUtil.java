package com.fzb.zrlog.plugin.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DevUtil {

    private static Properties prop = new Properties();

    static {
        try {
            InputStream in = DevUtil.class.getResourceAsStream("/dev.properties");
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String pluginHome() {
        Object home = prop.get("plugin.home");
        if (home != null) {
            return home.toString();
        }
         throw new RuntimeException("dev.properties setting error");
    }

    public static String blogVersion() {
        Object obj = prop.get("blog.version");
        if (obj != null) {
            return obj.toString();
        }
        throw new RuntimeException("dev.properties setting error");
    }

    public static String blogRuntimePath() {
        Object obj = prop.get("blog.runtimePath");
        if (obj != null) {
            return obj.toString();
        }
        throw new RuntimeException("dev.properties setting error");
    }
}
