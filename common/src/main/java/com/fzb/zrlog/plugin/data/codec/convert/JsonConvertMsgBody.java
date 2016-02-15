package com.fzb.zrlog.plugin.data.codec.convert;

import flexjson.JSONSerializer;

import java.nio.ByteBuffer;

/**
 * Created by xiaochun on 2016/2/13.
 */
public class JsonConvertMsgBody implements ConvertMsgBody {

    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        byte[] jsonByte = new JSONSerializer().deepSerialize(obj).getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(jsonByte.length);
        byteBuffer.put(jsonByte);
        System.out.println(new String(jsonByte));
        return byteBuffer;
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return null;
    }
}
