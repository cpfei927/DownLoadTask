package com.xueersi.downloadtask.downloadpool;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 *
 * @author ZouHao
 *         modify by shixiaoqiang
 */
public class MD5Utils {

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(String text) {
        MessageDigest msgDigest = null;

        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "System doesn't support MD5 algorithm.");
        }

        try {
            msgDigest.update(text.getBytes("utf-8"));

        } catch (UnsupportedEncodingException e) {

            throw new IllegalStateException(
                    "System doesn't support your  EncodingException.");

        }

        byte[] bytes = msgDigest.digest();

        String md5Str = new String(encodeHex(bytes));

        return md5Str;
    }

    /**
     * 获取文件Md5验证码
     *
     * @param fileName
     * @return
     */
    public static String md5sum(String fileName) {
        try {
            InputStream fis = new FileInputStream(fileName);
            return md5Stream(fis);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取文件Md5验证码
     *
     * @param fis fis
     * @return
     */
    public static String md5Stream(InputStream fis) {
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return new String(encodeHex(md5.digest()));
        } catch (Exception e) {
            return null;
        }
    }

    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }

    /**
     * 这是一个标准的MD5加密算法
     *
     * @param password 待加密原始字符串
     * @return
     */
    public static String disgest(String password) {
        try {
            //创建一个MD5加密的算法
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bs = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder(); //用来拼接转换后的十六进制数
            for (byte b : bs) {
                int i = b & 0xff; //拿一个15的int值与byte值相与后，将没有了符号位的负数
                //这里还可以这样 int i = (b&0xff) + 3   后面加上一个数，这叫做加盐操作，增大了解密难度
                String hexString = Integer.toHexString(i); //将int转换成16进制的字符串
                if (hexString.length() < 2) {
                    sb.append("0"); //有些因为第一个值是0，所以如果不足两个字符，在前面手动加一个0字符
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

}
