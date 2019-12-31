package com.licheedev.serialworkerdemo.serial;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Author by Winds on 2016/10/18.
 * Email heardown@163.com.
 */
public class ByteUtil {

    private static char forDigit(int digit, int radix) {
        if ((digit >= radix) || (digit < 0)) {
            return '\0';
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            return '\0';
        }
        if (digit < 10) {
            return (char) ('0' + digit);
        }
        return (char) ('A' - 10 + digit);
    }

    /**
     * 字节数组转换成对应的16进制表示的字符串
     *
     * @param src
     * @return
     */
    public static String bytes2HexStr(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = forDigit(src[i] & 0x0F, 16);
            builder.append(buffer);
        }
        return builder.toString();
    }

    /**
     * 十六进制字节数组转字符串
     *
     * @param src 目标数组
     * @param dec 起始位置
     * @param length 长度
     * @return
     */
    public static String bytes2HexStr(byte[] src, int dec, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(src, dec, temp, 0, length);
        return bytes2HexStr(temp);
    }

    /**
     * 16进制字符串转10进制数字
     *
     * @param hex
     * @return
     */
    public static long hexStr2decimal(String hex) {
        return Long.parseLong(hex, 16);
    }

    /**
     * 把十进制数字转换成足位的十六进制字符串,并补全空位
     *
     * @param num
     * @return
     */
    public static String decimal2fitHex(long num) {
        String hex = Long.toHexString(num).toUpperCase();
        if (hex.length() % 2 != 0) {
            return "0" + hex;
        }
        return hex.toUpperCase();
    }

    /**
     * 把十进制数字转换成足位的十六进制字符串,并补全空位
     *
     * @param num
     * @param strLength 字符串的长度
     * @return
     */
    public static String decimal2fitHex(long num, int strLength) {
        String hexStr = decimal2fitHex(num);
        StringBuilder stringBuilder = new StringBuilder(hexStr);
        while (stringBuilder.length() < strLength) {
            stringBuilder.insert(0, '0');
        }
        return stringBuilder.toString();
    }

    /**
     * 数字前补零
     *
     * @param dicimal
     * @param strLength
     * @return
     */
    public static String fitDecimalStr(int dicimal, int strLength) {
        StringBuilder builder = new StringBuilder(String.valueOf(dicimal));
        while (builder.length() < strLength) {
            builder.insert(0, "0");
        }
        return builder.toString();
    }

    /**
     * 字符串转十六进制字符串
     *
     * @param str
     * @return
     */
    public static String str2HexString(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = null;
        try {

            bs = str.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    /**
     * 把十六进制表示的字节数组字符串，转换成十六进制字节数组
     *
     * @param
     * @return byte[]
     */
    public static byte[] hexStr2bytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (hexChar2byte(achar[pos]) << 4 | hexChar2byte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * int类型转成高位在前的字节数组
     *
     * @param ori
     * @param arrayAmount 字节数组长度
     * @return
     */
    public static byte[] long2bytes(long ori, int arrayAmount) {
        byte[] bytes = new byte[arrayAmount];
        for (int i = 0; i < arrayAmount; i++) {
            // 高位在前
            bytes[i] = (byte) ((ori >>> (arrayAmount - i - 1) * 8) & 0xff);
        }
        return bytes;
    }

    /**
     * int类型转成高位在前的字节数组
     *
     * @param ori
     * @param arrayAmount 字节数组长度
     * @return
     */
    public static byte[] long2bytes(long ori, byte[] targetBytes, int offset, int arrayAmount) {
        for (int i = 0; i < arrayAmount; i++) {
            // 高位在前
            targetBytes[offset + i] = (byte) ((ori >>> (arrayAmount - i - 1) * 8) & 0xff);
        }
        return targetBytes;
    }

    /**
     * 字节数组（高位在前）转换成对应的非负整数
     *
     * @param ori 需要转换的字节数组
     * @param offset 目标位置偏移
     * @param len 目标数组长度
     * @return
     */
    public static long bytes2long(byte[] ori, int offset, int len) {
        long result = 0;
        for (int i = 0; i < len; i++) {
            result = result | ((0xffL & ori[offset + i]) << (len - 1 - i) * 8);
        }
        return result;
    }
    

    /**
     * 把16进制字符[0123456789abcde]（含大小写）转成字节
     *
     * @param c
     * @return
     */
    private static int hexChar2byte(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                return -1;
        }
    }

    /**
     * 转换二进制数值
     *
     * @param value 要转换的数值
     * @param byteLen 该值占用的字节大小
     * @param withDivider 是否每8个bit增加一个分隔符
     * @return
     */
    public static String toBinString(long value, int byteLen, boolean withDivider) {

        int bitLen = byteLen * 8;

        char[] chars = new char[bitLen];
        Arrays.fill(chars, '0');
        int charPos = bitLen;
        do {
            --charPos;
            if ((value & 1) > 0) {
                chars[charPos] = '1';
            }
            value >>>= 1;
        } while (value != 0 && charPos > 0);

        if (withDivider && byteLen > 1) {
            StringBuilder stringBuilder = new StringBuilder();

            boolean alreadyAppend = false;
            for (int i = 0; i < byteLen; i++) {
                if (alreadyAppend) {
                    stringBuilder.append(' ');
                } else {
                    alreadyAppend = true;
                }
                stringBuilder.append(chars, i * 8, 8);
            }

            return stringBuilder.toString();
        }

        return new String(chars);
    }

    /**
     * 转换二进制数值
     *
     * @param value 要转换的数值
     * @param byteLen 该值占用的字节大小
     * @return
     */
    public static String toBinString(long value, int byteLen) {
        return toBinString(value, byteLen, true);
    }

    /**
     * 异或校验和
     *
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static byte getXOR(byte[] bytes, int offset, int len) {
        // 计算校验和 
        byte toDiff = 0;
        // 校验和为除开校验位外的所有数据做异或
        for (int i = 0; i < len; i++) {
            toDiff = (byte) (toDiff ^ bytes[i + offset]);
        }
        return toDiff;
    }
}
