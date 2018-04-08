package com.zrlog.plugin.data.codec.convert;

import java.nio.ByteBuffer;

public class StringConvertMsgBody implements ConvertMsgBody {
    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        byte[] bytes = obj.toString().getBytes();
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return null;
    }
}
