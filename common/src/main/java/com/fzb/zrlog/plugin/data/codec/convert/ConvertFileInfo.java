package com.fzb.zrlog.plugin.data.codec.convert;

import com.fzb.common.util.HexaConversionUtil;
import com.fzb.zrlog.plugin.data.codec.FileDesc;
import com.fzb.zrlog.plugin.data.codec.FileInfo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.nio.ByteBuffer;

public class ConvertFileInfo {
    public static byte[] toByteArr(FileInfo fileInfo) {
        byte[] fileDescBytes = new JSONSerializer().deepSerialize(fileInfo.getFileDesc()).getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + fileDescBytes.length + 32 + 4 + fileInfo.getFileBytes().length);
        byteBuffer.put(HexaConversionUtil.intToByteArrayH(fileDescBytes.length));
        byteBuffer.put(fileDescBytes);
        byteBuffer.put(fileInfo.getMd5sum().getBytes());
        byteBuffer.put(HexaConversionUtil.intToByteArrayH(fileInfo.getFileBytes().length));
        byteBuffer.put(fileInfo.getFileBytes());
        return byteBuffer.array();
    }

    public static FileInfo toFileInfo(byte[] data) {
        FileInfo fileInfo = new FileInfo();
        int fileDescLength = HexaConversionUtil.byteArrayToIntH(HexaConversionUtil.subByts(data, 0, 4));
        FileDesc fileDesc = new JSONDeserializer<FileDesc>().deserialize(new String(HexaConversionUtil.subByts(data, 4, fileDescLength)));
        fileInfo.setFileDesc(fileDesc);
        fileInfo.setMd5sum(new String(HexaConversionUtil.subByts(data, fileDescLength + 4, 32)));
        fileInfo.setDataLength(HexaConversionUtil.byteArrayToIntH(HexaConversionUtil.subByts(data, fileDescLength + 4 + 32, 4)));
        fileInfo.setFileBytes(HexaConversionUtil.subByts(data, fileDescLength + 8 + 32, fileInfo.getDataLength()));
        return fileInfo;
    }
}
