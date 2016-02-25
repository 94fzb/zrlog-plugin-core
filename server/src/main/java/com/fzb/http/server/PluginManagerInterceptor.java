package com.fzb.http.server;

import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.mimetype.MimeTypeUtil;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginManagerInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerUtil.getLogger(PluginManagerInterceptor.class);

    public PluginManagerInterceptor() {
    }

    public boolean doInterceptor(HttpRequest request, HttpResponse response) {
        if (request.getUri().contains(".")) {
            InputStream in = PluginManagerInterceptor.class.getResourceAsStream(request.getUri());
            if (in == null) {
                response.renderCode(404);
            } else {
                String e = request.getUri().substring(request.getUri().lastIndexOf(".") + 1);
                response.addHeader("Content-Type", MimeTypeUtil.getMimeStrByExt(e));
                response.write(in);
            }
            return false;
        } else {
            Router router = request.getRequestConfig().getRouter();
            Method method;
            if (request.getUri().contains("-")) {
                method = router.getMethod(request.getUri().substring(0, request.getUri().indexOf("-")));
            } else {
                method = router.getMethod(request.getUri());
                if (method == null) {
                    String e = request.getUri().substring(0, request.getUri().lastIndexOf("/") + 1) + "index";
                    method = router.getMethod(e);
                }
            }

            LOGGER.info("invoke method " + method);
            if (method == null) {
                if (request.getUri().endsWith("/")) {
                    response.renderHtml(request.getUri() + "index.html");
                } else {
                    response.renderCode(404);
                }

                return false;
            } else {
                try {
                    Controller e2;
                    try {
                        Constructor e1 = method.getDeclaringClass().getConstructor(HttpRequest.class, HttpResponse.class);
                        e2 = (Controller) e1.newInstance(request, response);
                    } catch (NoSuchMethodException var7) {
                        e2 = (Controller) method.getDeclaringClass().newInstance();
                        e2.request = request;
                        e2.response = response;
                    }

                    method.invoke(e2);
                    return false;
                } catch (Exception var8) {
                    var8.printStackTrace();
                    LOGGER.log(Level.SEVERE, var8.getMessage());
                }

                return true;
            }
        }
    }
}