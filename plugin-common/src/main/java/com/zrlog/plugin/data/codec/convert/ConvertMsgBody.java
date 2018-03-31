package com.zrlog.plugin.data.codec.convert;

import java.nio.ByteBuffer;

/**
 * Created by xiaochun on 2016/2/13.
 */
public interface ConvertMsgBody {
    ByteBuffer toByteBuffer(Object obj);

    Object toObj(ByteBuffer byteBuffer);
}
