package com.fzb.zrlog.plugin.server;

import com.fzb.http.server.HttpRequest;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;

public class HttpMsgUtil {

    private HttpMsgUtil() {
    }

    public static HttpRequestInfo genInfo(HttpRequest request) {
        HttpRequestInfo msgBody = new HttpRequestInfo();
        msgBody.setFullUrl(request.getHeader("Full-Url"));
        msgBody.setUserName(request.getHeader("LoginUserName"));
        if (request.getHeader("LoginUserId") != null && !"".equals(request.getHeader("LoginUserId"))) {
            msgBody.setUserId(Integer.valueOf(request.getHeader("LoginUserId")));
        }
        msgBody.setVersion(request.getHeader("Blog-Version"));
        return msgBody;
    }
}
