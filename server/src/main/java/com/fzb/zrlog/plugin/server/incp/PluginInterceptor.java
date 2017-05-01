package com.fzb.zrlog.plugin.server.incp;

import com.fzb.common.util.IOUtil;
import com.fzb.common.util.RunConstants;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.data.codec.convert.FileConvertMsgBody;
import com.fzb.zrlog.plugin.server.config.PluginConfig;
import com.fzb.zrlog.plugin.server.util.HttpMsgUtil;
import com.fzb.zrlog.plugin.type.ActionType;
import com.fzb.zrlog.plugin.type.RunType;
import com.hibegin.http.server.api.HttpRequest;
import com.hibegin.http.server.api.HttpResponse;
import com.hibegin.http.server.api.Interceptor;
import com.hibegin.http.server.util.MimeTypeUtil;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class PluginInterceptor implements Interceptor {

    private static Logger LOGGER = LoggerUtil.getLogger(PluginInterceptor.class);

    @Override
    public boolean doInterceptor(HttpRequest httpRequest, final HttpResponse httpResponse) {
        if (httpRequest.getUri().contains("/")) {
            LOGGER.info("request uri " + httpRequest.getUri());
            String pluginName = httpRequest.getUri().substring(1);
            if (!pluginName.contains("/")) {
                httpResponse.renderCode(404);
                return false;
            }
            String path = pluginName.substring(pluginName.indexOf("/"));
            //LOGGER.log(Level.INFO, "request path " + path);
            pluginName = pluginName.substring(0, pluginName.indexOf("/"));
            //LOGGER.log(Level.INFO, "plugin name" + pluginName);
            boolean isLogin = (boolean) httpRequest.getAttr().get("isLogin");
            final IOSession session = PluginConfig.getInstance().getIOSessionByPluginName(pluginName);
            if (!isLogin && RunConstants.runType != RunType.DEV && (session == null || !session.getPlugin().getPaths().contains(path))) {
                httpResponse.renderCode(403);
                return false;
            }
            ActionType actionType = ActionType.HTTP_FILE;
            //Full Blog System ENV
            HttpRequestInfo msgBody = HttpMsgUtil.genInfo(httpRequest);
            msgBody.setUri(httpRequest.getUri().replace(pluginName + "/", ""));
            if (session != null) {
                if (("/".equals(msgBody.getUri()) || "".equals(msgBody.getUri())) && !"".equals(session.getPlugin().getIndexPage())) {
                    msgBody.setUri(session.getPlugin().getIndexPage());
                }
                if (!msgBody.getUri().contains(".")) {
                    msgBody.setUri(msgBody.getUri() + ".action");
                }
                if (msgBody.getUri().endsWith(".action")) {
                    actionType = ActionType.HTTP_METHOD;
                    msgBody.setHeader(httpRequest.getHeaderMap());
                    msgBody.setRequestBody(httpRequest.getContentByte());
                    msgBody.setParam(httpRequest.decodeParamMap());
                }
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
            } else {
                httpResponse.renderCode(404);
            }
        }
        return false;
    }
}
