package com.fzb.zrlog.plugin.data.codec.convert;

import java.nio.ByteBuffer;

public class StringConvertMsgBody implements ConvertMsgBody {
    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        byte[] bytes = obj.toString().getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        return byteBuffer;
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return null;
    }
}
