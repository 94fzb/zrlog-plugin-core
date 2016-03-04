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
                return true;
            } else {
                String e = request.getUri().substring(request.getUri().lastIndexOf(".") + 1);
                response.addHeader("Content-Type", MimeTypeUtil.getMimeStrByExt(e));
                response.write(in);
                return false;
            }
        } else {
            Router router = request.getRequestConfig().getRouter();
            Method method = null;
            if (request.getUri().contains("/")) {
                String uri = "/index";
                if (request.getUri().length() > 1) {
                    String pluginName = request.getUri().substring(1);
                    if (pluginName.lastIndexOf("/") == -1) {
                        uri = request.getUri();
                    } else {
                        pluginName = pluginName.substring(0, pluginName.lastIndexOf("/"));
                        // 不是一个插件名称，直接跳过检查
                        if (!pluginName.contains("/")) {
                            uri = request.getUri().substring(request.getUri().lastIndexOf("/"));
                            request.getParamMap().put("name", new String[]{pluginName});
                        }
                    }
                }
                method = router.getMethod(uri);
            }
            if (method == null) {
                return true;
            } else {
                try {
                    LOGGER.info("invoke method " + method);
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