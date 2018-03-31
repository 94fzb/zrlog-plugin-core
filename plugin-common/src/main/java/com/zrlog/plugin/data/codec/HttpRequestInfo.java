package com.zrlog.plugin.data.codec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/13.
 */
public class HttpRequestInfo {
    private String uri;
    private Map header;
    private byte[] requestBody;
    private Map<String, String[]> param;
    private String userName;
    private Integer userId;
    private String version;
    private String fullUrl;
    private String accessUrl;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullUrl() {
        if (fullUrl == null) {
            return uri;
        }
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
}
