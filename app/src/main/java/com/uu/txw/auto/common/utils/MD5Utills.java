package com.uu.txw.auto.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utills {
    /**
     * 32位MD5加密方法
     * 16位小写加密只需getMd5Value("xxx").substring(8, 24);即可
     *
     * @param sSecret
     * @return
     */
    public static String getMd5Value(byte[] bytes) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(bytes);
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();// 加密
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMd5Value(File filePath) {
            FileInputStream fis = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");

                // 创建输入流并读取文件内容
                fis = new FileInputStream(filePath);
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, length);
                }

                // 将字节数组转换为十六进制表示形式
                StringBuilder sb = new StringBuilder();
                for (byte b : md.digest()) {
                    sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
                }

                return sb.toString();
            } catch (Exception e) {
                return null;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignored) {}
                }
            }
        }
}
