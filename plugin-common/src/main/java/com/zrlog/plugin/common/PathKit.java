package com.zrlog.plugin.common;

import java.io.File;

/**
 * 提供给一些路径供程序更方便的调用
 *
 * @author Chun
 */
public class PathKit {

    public static String getConfPath() {
        return getRootPath() + "/conf/";
    }

    public static String getRootPath() {
        return System.getProperty("user.dir");
    }

    public static String getConfFile(String file) {
        return getConfPath() + file;
    }

    public static String getStaticPath() {
        return getRootPath() + "/static/";
    }

    public static String getTmpPath() {
        File file = new File(getRootPath() + "/tmp/");
        file.mkdirs();
        return file.toString();
    }
}
