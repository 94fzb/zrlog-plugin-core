package com.zrlog.plugincore.server.util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpUtils {


    public static byte[] sendGetRequest(String url, Map<String, String> headers) throws Exception {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            headers.forEach(builder::header);
            HttpRequest httpRequest = builder.GET().build();
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
        }
    }
}
