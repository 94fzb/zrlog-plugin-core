package com.zrlog.plugincore.server.handle;

import com.hibegin.common.util.IOUtil;
import com.hibegin.http.server.api.HttpErrorHandle;
import com.hibegin.http.server.api.HttpRequest;
import com.hibegin.http.server.api.HttpResponse;
import com.hibegin.http.server.util.MimeTypeUtil;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.data.codec.convert.FileConvertMsgBody;
import com.zrlog.plugin.type.ActionType;
import com.zrlog.plugin.type.RunType;
import com.zrlog.plugincore.server.config.PluginConfig;
import com.zrlog.plugincore.server.util.HttpMsgUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class PluginHandle implements HttpErrorHandle {

    private static final Logger LOGGER = LoggerUtil.getLogger(PluginHandle.class);

    private boolean includePath(Set<String> paths, String uri) {
        for (String path : paths) {
            if (RunConstants.runType == RunType.DEV) {
                LOGGER.log(Level.INFO, "path " + path + " uri " + uri);
            }
            String tPath = path.trim();
            if (!tPath.isEmpty()) {
                if (uri.startsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doHandle(HttpRequest httpRequest, HttpResponse httpResponse, Throwable e) {
        boolean isLogin = Boolean.parseBoolean(httpRequest.getHeader("IsLogin"));
        if (RunConstants.runType == RunType.DEV) {
            isLogin = true;
        }
        httpRequest.getAttr().put("isLogin", isLogin);
        String realUri = httpRequest.getUri().replaceFirst("/admin/plugins/", "");

        if (!realUri.contains("/")) {
            httpResponse.renderCode(404);
            return;
        }
        String pluginName = realUri.split("/")[0];
        if (RunConstants.runType == RunType.DEV) {
            LOGGER.log(Level.INFO, "plugin name " + pluginName);
        }
        final IOSession session = PluginConfig.getInstance().getIOSessionByPluginName(pluginName);
        if (Objects.isNull(session)) {
            httpResponse.renderCode(404);
            return;
        }
        String requestUri = realUri.replaceFirst(pluginName, "");
        if (!isLogin && RunConstants.runType != RunType.DEV && !includePath(session.getPlugin().getPaths(), httpRequest.getUri().replace("/" + session.getPlugin().getShortName(), ""))) {
            httpResponse.renderCode(403);
            return;
        }

        //Full Blog System ENV
        HttpRequestInfo msgBody = HttpMsgUtil.genInfo(httpRequest);
        msgBody.setUri(requestUri);
        if (("/".equals(msgBody.getUri()) || "".equals(msgBody.getUri())) && !"".equals(session.getPlugin().getIndexPage())) {
            msgBody.setUri(session.getPlugin().getIndexPage());
        }
        ActionType actionType;
        if (msgBody.getUri().endsWith(".")) {
            actionType = ActionType.HTTP_FILE;
        } else {
            actionType = ActionType.HTTP_METHOD;
            if (httpRequest.getRequestBodyByteBuffer() != null) {
                msgBody.setRequestBody(httpRequest.getRequestBodyByteBuffer().array());
            }
            msgBody.setUri(msgBody.getUri() + ".action");
        }
        msgBody.setHeader(httpRequest.getHeaderMap());
        msgBody.setParam(httpRequest.decodeParamMap());
        String fileExt = httpRequest.getUri().substring(httpRequest.getUri().lastIndexOf(".") + 1);
        int id = IdUtil.getInt();
        session.sendJsonMsg(msgBody, actionType.name(), id, MsgPacketStatus.SEND_REQUEST);
        String accessUrl = httpRequest.getHeader("AccessUrl");
        String cookie = httpRequest.getHeader("Cookie");
        if (accessUrl == null) {
            accessUrl = "";
        }
        if (cookie == null) {
            cookie = "";
        }
        session.getAttr().put("accessUrl", accessUrl);
        session.getAttr().put("cookie", cookie);
        MsgPacket responseMsgPacket = session.getResponseMsgPacketByMsgId(id);
        if (responseMsgPacket.getMethodStr().equals(ActionType.HTTP_ATTACHMENT_FILE.name())) {
            InputStream in = session.getPipeInByMsgId(id);
            File file = new FileConvertMsgBody().toFile(IOUtil.getByteByInputStream(in));
            httpResponse.renderFile(file);
        } else {
            String ext = fileExt;
            if (responseMsgPacket.getContentType() == ContentType.JSON) {
                ext = "json";
            } else if (responseMsgPacket.getContentType() == ContentType.HTML) {
                ext = "html";
            }
            InputStream in = session.getPipeInByMsgId(id);
            httpResponse.addHeader("Content-Type", MimeTypeUtil.getMimeStrByExt(ext));
            httpResponse.write(in, 200);
        }
    }
}
