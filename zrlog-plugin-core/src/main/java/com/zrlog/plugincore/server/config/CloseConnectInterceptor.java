package com.zrlog.plugincore.server.config;

import com.hibegin.http.server.api.HttpRequest;
import com.hibegin.http.server.api.HttpResponse;
import com.hibegin.http.server.api.Interceptor;

public class CloseConnectInterceptor implements Interceptor {
    @Override
    public boolean doInterceptor(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        if(httpRequest.getUri().startsWith("/admin/plugins/static/")){
            httpResponse.addHeader("Cache-Control", "max-age=31536000, immutable"); // 1 年的秒数
        }
        httpResponse.addHeader("Connection", "close");
        return true;
    }
}
