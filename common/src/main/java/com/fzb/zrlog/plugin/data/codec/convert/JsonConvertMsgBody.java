package com.fzb.zrlog.plugin.data.codec.convert;

import flexjson.JSONDeserializer;
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
        return byteBuffer;
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return null;
    }

    public <T> T toObj(ByteBuffer byteBuffer, Class<T> clazz) {
        String jsonStr = new String(byteBuffer.array());
        return new JSONDeserializer<T>().deserialize(jsonStr, clazz);
    }
}
