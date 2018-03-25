package com.fzb.zrlog.plugin.render;

import com.fzb.zrlog.plugin.message.Plugin;

import java.io.InputStream;
import java.util.Map;

public interface IRenderHandler {

    String render(String templatePath, Plugin plugin, Map<String, Object> map);

    String render(InputStream inputStream, Plugin plugin, Map<String, Object> map);
}
