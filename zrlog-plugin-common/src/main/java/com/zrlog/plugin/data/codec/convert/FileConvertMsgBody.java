package com.zrlog.plugin.data.codec.convert;

import com.google.gson.Gson;
import com.hibegin.common.util.IOUtil;
import com.hibegin.common.util.SecurityUtils;
import com.zrlog.plugin.common.HexaConversionUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.data.codec.FileDesc;
import com.zrlog.plugin.data.codec.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class FileConvertMsgBody implements ConvertMsgBody {

    private static final Logger LOGGER = LoggerUtil.getLogger(FileConvertMsgBody.class);

    private static ByteBuffer toByteArr(FileInfo fileInfo) {
        byte[] fileDescBytes = new Gson().toJson(fileInfo.getFileDesc()).getBytes();
        return ByteBuffer.wrap(HexaConversionUtil.mergeBytes(HexaConversionUtil.intToByteArrayH(fileDescBytes.length), fileDescBytes,
                fileInfo.getMd5sum().getBytes(), HexaConversionUtil.intToByteArrayH(fileInfo.getFileBytes().length), fileInfo.getFileBytes()));
    }

    private static FileInfo toFileInfo(byte[] data) {
        FileInfo fileInfo = new FileInfo();
        int fileDescLength = HexaConversionUtil.byteArrayToIntH(HexaConversionUtil.subByts(data, 0, 4));
        FileDesc fileDesc = new Gson().fromJson(new String(HexaConversionUtil.subByts(data, 4, fileDescLength)), FileDesc.class);
        fileInfo.setFileDesc(fileDesc);
        fileInfo.setMd5sum(new String(HexaConversionUtil.subByts(data, fileDescLength + 4, 32)));
        fileInfo.setDataLength(HexaConversionUtil.byteArrayToIntH(HexaConversionUtil.subByts(data, fileDescLength + 4 + 32, 4)));
        fileInfo.setFileBytes(HexaConversionUtil.subByts(data, fileDescLength + 8 + 32, fileInfo.getDataLength()));
        return fileInfo;
    }

    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        if (obj instanceof File) {
            File file = (File) obj;
            FileInfo fileInfo = new FileInfo();
            byte[] fileBytes = new byte[0];
            try {
                fileBytes = IOUtil.getByteByInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                LOGGER.warning(e.getMessage());
            }

            FileDesc fileDesc = new FileDesc();
            fileDesc.setFileName(file.getName());
            fileDesc.setFilePath(file.getParent());
            fileInfo.setFileDesc(fileDesc);

            fileInfo.setDataLength(fileBytes.length);
            fileInfo.setFileBytes(fileBytes);
            fileInfo.setMd5sum(SecurityUtils.md5(fileBytes));
            return toByteArr(fileInfo);
        } else {
            throw new RuntimeException("obj not a file " + obj);
        }
    }

    @Override
    public Object toObj(ByteBuffer byteBuffer) {
        return toFileInfo(byteBuffer.array());
    }

    public File toFile(byte[] bytes) {
        FileInfo fileInfo = toFileInfo(bytes);
        return new File(fileInfo.getFileDesc().getFilePath() + "/" + fileInfo.getFileDesc().getFileName());
    }
}
