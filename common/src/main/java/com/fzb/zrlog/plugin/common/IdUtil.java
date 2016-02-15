package com.fzb.zrlog.plugin.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class IdUtil {

    private static AtomicInteger msgIds = new AtomicInteger();

    public static int getInt() {
        return msgIds.incrementAndGet();
    }
}
