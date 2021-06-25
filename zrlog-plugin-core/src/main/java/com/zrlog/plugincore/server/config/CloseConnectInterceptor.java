package com.zrlog.plugincore.server.config;

import com.hibegin.http.server.api.HttpRequest;
import com.hibegin.http.server.api.HttpResponse;
import com.hibegin.http.server.api.Interceptor;

public class CloseConnectInterceptor implements Interceptor {
    @Override
    public boolean doInterceptor(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        httpResponse.addHeader("Connection", "close");
        return true;
    }
}
