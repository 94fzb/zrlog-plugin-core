package com.fzb.common.util.http;

import com.fzb.common.util.http.handle.HttpFileHandle;
import com.fzb.common.util.http.handle.HttpHandle;
import com.fzb.common.util.http.handle.HttpStringHandle;
import com.fzb.zrlog.plugin.common.LoggerUtil;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUtil {
    private static Logger LOGGER = LoggerUtil.getLogger(HttpUtil.class);
    private static CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    private static HttpPost postForm(String urlPath, Map<String, String[]> params) {
        HttpPost httPost = new HttpPost(urlPath);
        List nvps = new ArrayList();
        if (params == null) {
            return httPost;
        }
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            for (String string : params.get(key)) {
                try {
                    nvps.add(new BasicNameValuePair(key, URLDecoder.decode(string, "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE,"",e);
                }
            }
        }
        try {
            httPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,"",e);
        }
        return httPost;
    }

    private static HttpPost postForm(String urlPath, byte[] data) {
        HttpPost httpost = new HttpPost(urlPath);
        httpost.setEntity(new ByteArrayEntity(data));
        return httpost;
    }

    private static String mapToQueryStr(Map<String, String[]> params) {
        String queryStr = "";
        if ((params != null) && (!params.isEmpty())) {
            queryStr = queryStr + "?";
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                for (String string : params.get(key)) {
                    queryStr += key + "=" + string + "&";
                }
            }
            queryStr = queryStr.substring(0, queryStr.length() - 1);
        }
        return queryStr;
    }

    private static void setHttpHeaders(HttpRequestBase header, Map<String, String> reqheaders) {
        // set default http header
        header.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        header.setHeader("Accept-Charset", "GB2312,UTF-8;q=0.7,*;q=0.7");
        header.setHeader("Accept-Encoding", "gzip, deflate");
        header.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        //header.setHeader("Connection", "keep-alive");
        //TODO 根据操作系统选择 Usage
        header.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:41.0) Gecko/20100101 Firefox/41.0");

        for (Map.Entry<String, String> reqHeader : reqheaders.entrySet()) {
            header.setHeader(reqHeader.getKey(), reqHeader.getValue());
            LOGGER.info("key " + reqHeader.getKey() + " value-> " + reqHeader.getValue());
        }
    }

    public static <T> HttpHandle<? extends T> sendPostRequest(String urlPath, Map<String, String[]> params,
                                                              HttpHandle<T> httpHandle, Map<String, String> reqHeaders)
            throws IOException, InstantiationException {
        LOGGER.info(urlPath + " http post params " + params);
        return sendRequest(postForm(urlPath, params), httpHandle, reqHeaders);
    }

    public static <T> HttpHandle<? extends T> sendPostRequest(String urlPath, byte[] date,
                                                              HttpHandle<T> httpHandle, Map<String, String> reqHeaders)
            throws IOException, InstantiationException {
        reqHeaders.remove("Content-Length");
        return sendRequest(postForm(urlPath, date), httpHandle, reqHeaders);
    }


    public static <T> HttpHandle<? extends T> sendRequest(HttpRequestBase httpRequestBase, HttpHandle<T> httpHandle, Map<String, String> reqheaders)
            throws IOException {
        setHttpHeaders(httpRequestBase, reqheaders);
        CloseableHttpResponse response = httpClient.execute(httpRequestBase);
        boolean needClose = httpHandle.handle(httpRequestBase, response);
        if (needClose) {
            response.close();
        }
        return httpHandle;
    }

    public static <T> HttpHandle<? extends T> sendGetRequest(String urlPath, Map<String, String[]> requestParam, HttpHandle<T> httpHandle, Map<String, String> reqheaders)
            throws IOException {
        String queryStr = mapToQueryStr(requestParam);
        if (queryStr.length() > 0) {
            queryStr = queryStr.substring(1);
        }
        URI uri;
        try {
            URL url = new URL(urlPath);
            if (queryStr.length() == 0) {
                queryStr = url.getQuery() != null ? URLDecoder.decode(url.getQuery(), "UTF-8") : null;
            }
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), queryStr, url.getRef());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpGet httpGet = new HttpGet(uri.toASCIIString());
        return sendRequest(httpGet, httpHandle, reqheaders);
    }

    public static <T> HttpHandle<? extends T> sendGetRequest(String urlPath, HttpHandle<T> httpHandle, Map<String, String> reqheaders)
            throws IOException {
        return sendGetRequest(urlPath, null, httpHandle, reqheaders);
    }


    public static void main(String[] args)
            throws IOException {
        String urlStr = "http://store.zrlog.com/plugin/download?id=1";
        HttpFileHandle fileHandle = (HttpFileHandle) HttpUtil.sendGetRequest(urlStr,
                new HttpFileHandle("/home/xiaochun"), new HashMap<String, String>());
    }

    public static String getTextByUrl(String url) throws IOException {
        return sendGetRequest(url, new HttpStringHandle(), new HashMap<String, String>()).getT();
    }
}
