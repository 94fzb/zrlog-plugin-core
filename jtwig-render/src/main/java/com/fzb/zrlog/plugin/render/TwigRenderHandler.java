package com.fzb.zrlog.plugin.render;

import com.fzb.common.util.IOUtil;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.message.Plugin;
import com.lyncode.jtwig.JtwigModelMap;
import com.lyncode.jtwig.JtwigTemplate;
import com.lyncode.jtwig.exception.CompileException;
import com.lyncode.jtwig.exception.ParseException;
import com.lyncode.jtwig.exception.RenderException;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwigRenderHandler implements IRenderHandler {

    private static final Logger LOGGER = LoggerUtil.getLogger(TwigRenderHandler.class);

    @Override
    public String render(String templatePath, Plugin plugin, Map<String, Object> map) {
        return render(TwigRenderHandler.class.getResourceAsStream(templatePath), plugin, map);
    }

    @Override
    public String render(InputStream inputStream, Plugin plugin, Map<String, Object> map) {
        JtwigTemplate jtwigTemplate = JtwigTemplate.fromString(IOUtil.getStringInputStream(inputStream));
        JtwigModelMap modelMap = new JtwigModelMap();
        try {
            map.put("_plugin", plugin);
            modelMap.putAll(map);
            return jtwigTemplate.output(modelMap);
        } catch (ParseException | CompileException | RenderException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
        return "<html><body>Not Found</body></html>";
    }
}
