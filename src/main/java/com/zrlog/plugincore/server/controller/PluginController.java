package com.zrlog.plugincore.server.controller;


import com.google.gson.Gson;
import com.hibegin.http.server.util.PathUtil;
import com.hibegin.http.server.web.Controller;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.util.BooleanUtils;
import com.zrlog.plugincore.server.util.FileUtils;
import com.zrlog.plugincore.server.util.HttpMsgUtil;
import com.zrlog.plugincore.server.util.PluginUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class PluginController extends Controller {

    private final java.util.logging.Logger LOGGER = LoggerUtil.getLogger(PluginController.class);

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

    private String getBasePath() {
        String fullUrl = request.getHeader("Full-Url");
        if (fullUrl == null) {
            return request.getUri().substring(0, request.getUri().lastIndexOf("/"));
        } else {
            return URI.create(fullUrl).getPath();
        }
    }

    public void download() {
        String downloadUrl = getRequest().getParaToStr("downloadUrl");
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        String pluginName = fileName.substring(0, fileName.indexOf("."));
        try {
            File path = new File(PluginConfig.getInstance().getPluginBasePath());
            File file = new File(path + "/" + fileName);
            if (file.exists()) {
                response.redirect(getBasePath() + "/downloadResult?message=" + URLEncoder.encode("插件已经存在了", StandardCharsets.UTF_8) +
                        "&pluginName=" + pluginName);
                return;
            }
            File pluginFile = PluginUtil.downloadPluginByUrl(downloadUrl, fileName);
            PluginUtil.loadPlugin(pluginFile);
            response.redirect(getBasePath() + "/downloadResult?message=" + URLEncoder.encode("下载插件成功", StandardCharsets.UTF_8) +
                    "&pluginName=" + pluginName);
        } catch (Exception e) {
            response.redirect(getBasePath() + "/downloadResult?message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8) +
                    "&pluginName=" + pluginName);
            LOGGER.log(Level.FINER, "download error ", e);
        }
    }


    public void service() {
        String name = getRequest().getParaToStr("name");
        if (name != null && !"".equals(name)) {
            IOSession session = PluginConfig.getInstance().getIOSessionByService(name);
            if (session != null) {
                int msgId = session.requestService(name, request.decodeParamMap());
                getResponse().addHeader("Content-Type", "application/json");
                getResponse().write(session.getPipeInByMsgId(msgId));
            } else {
                getResponse().renderCode(404);
            }
        }
    }


    private HttpRequestInfo genInfo() {
        return HttpMsgUtil.genInfo(getRequest());
    }

    public void upload() {
        Map<String, Object> map = new HashMap<>();
        File file = getRequest().getFile("file");
        String finalFile = PathUtil.getStaticPath() + file.getName() + "." + getRequest().getParaToStr("ext");
        FileUtils.moveOrCopyFile(file.toString(), finalFile, true);
        map.put("url", getBasePath() + "/" + new File(finalFile).getName());
        response.renderJson(map);
    }
}
