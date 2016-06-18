package com.fzb.zrlog.plugin.server.util;

import com.fzb.http.kit.StringsUtil;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.session.HttpSession;
import com.fzb.zrlog.plugin.message.Plugin;
import com.fzb.zrlog.plugin.render.FreeMarkerRenderHandler;

public class ServerFreeMarkerReaderHandler extends FreeMarkerRenderHandler {

    public String render(String templatePath, HttpRequest httpRequest) {
        try {
            HttpSession httpSession = httpRequest.getSession();
            if (httpSession != null) {
                httpRequest.getAttr().put("session", httpSession);
                httpRequest.getAttr().put("request", httpRequest);
            }
            Plugin plugin = new Plugin();
            return render(templatePath, plugin, httpRequest.getAttr());
        } catch (Exception var6) {
            var6.printStackTrace();
            return StringsUtil.getHtmlStrByStatusCode(404);
        }

    }
}
