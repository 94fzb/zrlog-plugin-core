package com.fzb.zrlog.plugin.server;

import com.fzb.common.util.RunConstants;
import com.fzb.http.server.HttpRequest;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.type.RunType;

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
        if (RunConstants.runType == RunType.DEV) {
            msgBody.setFullUrl(request.getScheme() + "://" + request.getHeader("Host") + request.getUri());
            msgBody.setUserName("LOC_DEV");
            msgBody.setUserId(1);
        }
        return msgBody;
    }
}
