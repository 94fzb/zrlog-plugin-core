package com.fzb.common.util;

import com.fzb.zrlog.plugin.common.LoggerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Md5Util {

    private static final Logger LOGGER = LoggerUtil.getLogger(Md5Util.class);

    public static String MD5(String pwd) {
        return MD5(pwd.getBytes());
    }

    public static String MD5(File file) throws IOException {
        return MD5(IOUtil.getByteByInputStream(new FileInputStream(file)));
    }

    public static String MD5(byte[] bytes) {
        if (bytes.length > 0) {
            char[] md5String = {'0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            try {
                byte[] btInput = bytes;
                MessageDigest mdInst = MessageDigest.getInstance("MD5");
                mdInst.update(btInput);

                byte[] md = mdInst.digest();
                int j = md.length;

                char[] str = new char[j * 2];
                int k = 0;
                for (int i = 0; i < j; i++) {
                    byte byte0 = md[i];
                    str[(k++)] = md5String[(byte0 >>> 4 & 0xF)];
                    str[(k++)] = md5String[(byte0 & 0xF)];
                }

                return new String(str).toLowerCase();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
                return "";
            }
        }
        return "d41d8cd98f00b204e9800998ecf8427e";
    }

    public static void main(String[] args) throws IOException {
        System.out.println(MD5("w123456"));
        System.out.println(MD5(new File("/home/xiaochun/sync-backup/backup")));
        /*System.out.println(System.currentTimeMillis());
        System.out.println(MD5(UUID.randomUUID().toString()));
		System.out.println(System.currentTimeMillis());*/
    }
}