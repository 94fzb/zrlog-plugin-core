package com.zrlog.plugin.common;

import java.util.Random;

/**
 * Created by xiaochun on 2016/2/12.
 */
public class IdUtil {

    public static int getInt() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }
}
