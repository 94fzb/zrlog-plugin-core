package com.fzb.common.util.http.handle;

import com.fzb.common.util.IOUtil;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpStringHandle extends HttpHandle<String> {

    private static final Logger LOGGER = LoggerUtil.getLogger(HttpStringHandle.class);

    @Override
    public boolean handle(HttpRequestBase request, HttpResponse response) {
        try {
            setT(IOUtil.getStringInputStream(response.getEntity().getContent()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
        return true;
    }
}
