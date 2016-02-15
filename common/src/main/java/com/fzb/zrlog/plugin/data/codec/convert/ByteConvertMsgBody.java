package com.fzb.zrlog.plugin.data.codec.convert;

import java.nio.ByteBuffer;

/**
 * Created by xiaochun on 2016/2/13.
 */
public class ByteConvertMsgBody implements ConvertMsgBody {

    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        if (obj instanceof ByteBuffer) {
            return (ByteBuffer) obj;
        } else if (obj instanceof byte[]) {
            ByteBuffer data = ByteBuffer.allocate(((byte[]) obj).length);
            data.put(((byte[]) obj));
            return data;
        }
        return null;
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return null;
    }
}
