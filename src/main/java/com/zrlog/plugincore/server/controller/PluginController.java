package com.zrlog.plugincore.server.controller;


import com.google.gson.Gson;
import com.hibegin.http.server.web.Controller;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.util.BooleanUtils;
import com.zrlog.plugincore.server.util.HttpMsgUtil;
import com.zrlog.plugincore.server.util.PluginUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginController extends Controller {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginController.class);

    /**
     * 得到插件列表
     */
    public void index() throws IOException {
        Document document = Jsoup.parse(Objects.requireNonNull(PluginController.class.getResourceAsStream("/static/index.html")), "UTF-8", "");
        document.title("插件管理");
        document.body().removeAttr("class");
        if (BooleanUtils.isTrue(getRequest().getHeader("Dark-Mode"))) {
            document.body().addClass("dark");
        } else {
            document.body().addClass("light");
        }
        String jsonStr = new Gson().toJson(new PluginApiController(request, response).plugins());
        Element pluginInfo = document.getElementById("pluginInfo");
        if (Objects.nonNull(pluginInfo)) {
            pluginInfo.text(jsonStr);
        }
        response.renderHtmlStr(document.html());
    }

    public void downloadResult() throws IOException {
        index();
    }

    public void pluginStarted() throws IOException {
        index();
    }


    public void download() {
        String downloadUrl = getRequest().getParaToStr("downloadUrl");
        if (Objects.isNull(downloadUrl)) {
            return;
        }
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        String pluginName = PluginUtil.getPluginName(new File(fileName));
        try {
            File path = new File(PluginConfig.getInstance().getPluginBasePath());
            File file = new File(path + "/" + fileName);
            if (file.exists()) {
                response.redirect("/admin/plugins/downloadResult?message=" + URLEncoder.encode("插件已经存在了", StandardCharsets.UTF_8) +
                        "&pluginName=" + pluginName);
                return;
            }
            File pluginFile = PluginUtil.downloadPlugin(PluginUtil.getPluginFile(pluginName).getName());
            PluginUtil.loadPlugin(pluginFile, UUID.randomUUID().toString());
            response.redirect("/admin/plugins/downloadResult?message=" + URLEncoder.encode("下载插件成功", StandardCharsets.UTF_8) +
                    "&pluginName=" + pluginName);
        } catch (Exception e) {
            response.redirect("/admin/plugins/downloadResult?message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8) +
                    "&pluginName=" + pluginName);
            LOGGER.log(Level.FINER, "download error ", e);
        }
    }


    public void service() {
        String name = getRequest().getParaToStr("name");
        if (name != null && !name.isEmpty()) {
            IOSession session = PluginConfig.getInstance().getIOSessionByService(name);
            if (session != null) {
                int msgId = session.requestService(name, request.decodeParamMap());
                getResponse().addHeader("Content-Type", "application/json");
                ByteArrayInputStream bin = new ByteArrayInputStream(session.getResponseMsgPacketByMsgId(msgId).getData().array());
                getResponse().write(bin);
            } else {
                getResponse().renderCode(404);
            }
        }
    }


    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }

    public void upload() {
        /*Map<String, Object> map = new HashMap<>();
        File file = getRequest().getFile("file");
        String finalFile = PathUtil.getStaticPath() + file.getName() + "." + getRequest().getParaToStr("ext");
        FileUtils.moveOrCopyFile(file.toString(), finalFile, true);
        map.put("url", getBasePath() + "/" + new File(finalFile).getName());
        response.renderJson(map);*/
        response.renderJson(new HashMap<>());
    }
}
