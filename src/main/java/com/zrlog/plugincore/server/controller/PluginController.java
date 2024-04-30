package com.zrlog.plugincore.server.controller;


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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
        String basePath;
        if (fullUrl == null) {
            basePath = request.getUrl().substring(0, request.getUrl().lastIndexOf("/"));
        } else {
            if (fullUrl.contains("?")) {
                fullUrl = fullUrl.substring(0, fullUrl.indexOf("?"));
            }
            basePath = fullUrl.substring(0, fullUrl.lastIndexOf("/"));
        }
        return basePath;
    }

    public void download() throws UnsupportedEncodingException {
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
