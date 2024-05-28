package com.zrlog.plugincore.server;

import com.hibegin.common.util.Pid;
import com.hibegin.http.server.util.NativeImageUtils;
import com.hibegin.http.server.util.PathUtil;

import java.io.File;
import java.io.IOException;

public class GraalvmAgentApplication {

    public static void main(String[] args) throws IOException {
        Application.init();
        Pid.get();
        String basePath = System.getProperty("user.dir").replace("/target", "");
        PathUtil.setRootPath(basePath);
        System.out.println("basePath = " + basePath);
        File file = new File(basePath + "/src/main/frontend/build");
        NativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath(), "/static");
        Application.nativeAgent = true;
        Application.main(args);
    }
}