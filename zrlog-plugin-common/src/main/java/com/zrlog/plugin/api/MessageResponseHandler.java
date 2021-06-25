package com.zrlog.plugin.api;

public interface MessageResponseHandler<T> {
    void handler(T t);
}
