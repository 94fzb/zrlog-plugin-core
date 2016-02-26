package com.fzb.zrlog.plugin.server.incp;

import com.fzb.http.mimetype.MimeTypeUtil;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.Interceptor;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.server.DataMap;
import com.fzb.zrlog.plugin.type.ActionType;

import java.io.InputStream;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class PluginInterceptor implements Interceptor {
    @Override
    public boolean doInterceptor(HttpRequest httpRequest, final HttpResponse httpResponse) {
        if (httpRequest.getUri().contains("/")) {
            String pluginName = httpRequest.getUri().substring(1);
            pluginName = pluginName.substring(0, pluginName.indexOf("/"));
            ActionType actionType = ActionType.HTTP_FILE;
            HttpRequestInfo msgBody = new HttpRequestInfo();
            msgBody.setUri(httpRequest.getUri().replace(pluginName + "/", ""));
            msgBody.setUrl(httpRequest.getFullUrl());
            final IOSession session = DataMap.getPluginMap().get(pluginName);
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
                msgBody.setParam(httpRequest.getParamMap());
            }
            String fileExt = httpRequest.getUri().substring(httpRequest.getUri().lastIndexOf(".") + 1);
            if (session != null) {
                int id = IdUtil.getInt();
                session.sendJsonMsg(msgBody, actionType.name(), id, MsgPacketStatus.SEND_REQUEST);
                MsgPacket responseMsgPacket = session.getResponseMsgPacketByMsgId(id);
                String ext = fileExt;
                if (responseMsgPacket.getContentType() == ContentType.JSON) {
                    ext = "json";
                }
                else if (responseMsgPacket.getContentType() == ContentType.HTML) {
                    ext = "html";
                }
                InputStream in = session.getPipeInByMsgId(id);
                httpResponse.addHeader("Content-Type", MimeTypeUtil.getMimeStrByExt(ext));
                httpResponse.write(in, 200);
            } else {
                httpResponse.renderCode(404);
            }
        }
        return false;
    }
}
