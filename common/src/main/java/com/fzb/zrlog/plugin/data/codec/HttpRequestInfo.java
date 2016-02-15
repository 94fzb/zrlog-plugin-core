package com.fzb.zrlog.plugin.data.codec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/13.
 */
public class HttpRequestInfo {
    private String uri;
    private String url;
    private Map header;
    private byte[] requestBody;
    private Map<String, String[]> param;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map getHeader() {
        return header;
    }

    public void setHeader(Map header) {
        this.header = header;
    }

    public byte[] getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, String[]> getParam() {
        return param;
    }

    public void setParam(Map<String, String[]> param) {
        this.param = param;
    }

    public Map simpleParam() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : param.entrySet()) {
            if (entry.getValue().length == 1) {
                map.put(entry.getKey(), entry.getValue()[0]);
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
}
