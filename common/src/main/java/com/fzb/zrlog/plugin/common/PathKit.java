package com.fzb.zrlog.plugin.common;

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
        String path;
        if (PathKit.class.getResource("/") != null) {
            path = new File(PathKit.class.getClass().getResource("/").getPath()).getParentFile().getParentFile().toString();

        } else {
            String thisPath = PathKit.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("\\", "/");
            if ("/".equals(File.separator)) {
                path = thisPath.substring(0, thisPath.lastIndexOf('/'));
            } else {
                path = thisPath.substring(1, thisPath.lastIndexOf('/'));
            }
        }
        return path;
    }

    public static String getConfFile(String file) {
        return getConfPath() + file;
    }

    public static String getStaticPath() {
        return getRootPath() + "/static/";
    }
}
