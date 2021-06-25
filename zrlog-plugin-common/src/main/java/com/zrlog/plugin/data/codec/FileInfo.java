package com.zrlog.plugin.data.codec;

public class FileInfo {
    private int dataLength;
    private String md5sum;
    private FileDesc fileDesc;
    private byte[] fileBytes;

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public FileDesc getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(FileDesc fileDesc) {
        this.fileDesc = fileDesc;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
