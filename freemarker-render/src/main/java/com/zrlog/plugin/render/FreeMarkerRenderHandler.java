package com.zrlog.plugin.render;

import com.hibegin.common.util.IOUtil;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.PathKit;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.type.RunType;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreeMarkerRenderHandler implements IRenderHandler {

    private static Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
    private static String devEnvPath = PathKit.getRootPath() + "/src/main/resources";
    private static Logger LOGGER = LoggerUtil.getLogger(FreeMarkerRenderHandler.class);

    @Override
    public String render(String templatePath, Plugin plugin, Map<String, Object> map) {
        InputStream inputStream;
        if (RunConstants.runType == RunType.DEV && new File(devEnvPath + templatePath).exists()) {
            try {
                inputStream = new FileInputStream(devEnvPath + templatePath);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "not found file ", e);
                return LoggerUtil.recordStackTraceMsg(e);
            }
        } else {
            inputStream = FreeMarkerRenderHandler.class.getResourceAsStream(templatePath);
        }
        return render(inputStream, plugin, map);
    }

    @Override
    public String render(InputStream inputStream, Plugin plugin, Map<String, Object> map) {
        try {
            Template e = new Template(null, new StringReader(IOUtil.getStringInputStream(inputStream)), cfg);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            map.put("_plugin", plugin);
            e.process(map, writer);
            writer.flush();
            writer.close();
            return new String(out.toByteArray());
        } catch (Exception var6) {
            var6.printStackTrace();
            return "";
        }
    }
}
