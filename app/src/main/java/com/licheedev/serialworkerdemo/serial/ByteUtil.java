package com.licheedev.serialworkerdemo.serial;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Author by Winds on 2016/10/18.
 * Email heardown@163.com.
 */
public class ByteUtil {

    //public static void main(String[] args) {
    //    byte[] bytes = {
    //        (byte) 0xab, 0x01, 0x11
    //    };
    //    String hexStr = bytes2HexStr(bytes);
    //    System.out.println(hexStr);
    //    System.out.println(hexStr2decimal(hexStr));
    //    System.out.println(decimal2fitHex(570));
    //    String adc = "abc";
    //    System.out.println(str2HexString(adc));
    //    System.out.println(bytes2HexStr(adc.getBytes()));
    //}

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
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            builder.append(buffer);
        }
        return builder.toString().toUpperCase();
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
     * int转2字节的byte数组 高字节在前低字节在后
     *
     * @param value
     * @return
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static String getXOR(String hex) {
        if (hex.length() == 0) {
            return null;
        }
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        int or = 0;
        for (int i = 0, size = hex.length(); i < size; i = i + 2) {
            String subHex = hex.substring(i, i + 2);
            or = or ^ Integer.parseInt(subHex, 16);
        }
        String xor = Integer.toHexString(or) + "";
        if (xor.length() == 1) {
            xor = "0" + xor;
        }
        return xor;
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

    public static String HexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(
                Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static float BinaryStringToFloat(String binaryString) {
        // float是32位，将这个binaryString左边补0补足32位，如果是Double补足64位。
        String stringValue = LeftPad(binaryString, '0', 32);
        // 首位是符号部分，占1位。
        // 如果符号位是0则代表正数，1代表负数
        int sign = stringValue.charAt(0) == '0' ? 1 : -1;
        // 第2到9位是指数部分，float占8位，double占11位。
        String exponentStr = stringValue.substring(1, 9);
        // 将这个二进制字符串转成整数，由于指数部分加了偏移量（float偏移量是127，double是1023）
        // 所以实际值要减去127
        int exponent = Integer.parseInt(exponentStr, 2) - 127;
        // 最后的23位是尾数部分，由于规格化数，小数点左边隐含一个1，现在加上
        String mantissaStr = "1".concat(stringValue.substring(9, 32));
        // 这里用double，尽量保持精度，最好用BigDecimal，这里只是方便计算所以用double
        double mantissa = 0.0;

        for (int i = 0; i < mantissaStr.length(); i++) {
            final int intValue = Character.getNumericValue(mantissaStr.charAt(i));
            // 计算小数部分，具体请查阅二进制小数转10进制数相关资料
            mantissa += (intValue * Math.pow(2, -i));
        }
        // 根据IEEE 754 标准计算：符号位 * 2的指数次方 * 尾数部分
        return (float) (sign * Math.pow(2, exponent) * mantissa);
    }

    private static String LeftPad(final String str, final char padChar, int length) {
        final int repeat = length - str.length();

        if (repeat <= 0) {
            return str;
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf).concat(str);
    }

    /*--------------------------------------------------------------*/

    /**
     * 含有1字节的负数16进制转的10进制数据
     *
     * @param num 一字节的16进制数据
     */
    public static String get10(String num) {
        long number = Long.parseLong(num, 16);
        if (number > 50) {
            long b = number - 255;
            return String.valueOf(b);
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * 含有1字节的负数16进制转的10进制数据
     *
     * @param num 一字节的16进制数据
     */
    public static String get10(String num, int length) {

        String s2 = String.valueOf(Long.parseLong(num, 16));
        if (s2.length() == length) {
            return s2;
        } else {
            if (s2.length() > 4) {
                return String.valueOf((Integer.parseInt(s2) - 65535));
            } else {
                String t1 = s2;
                int i = length - t1.length();
                for (int j = 0; j < i; j++) {
                    t1 = "0" + t1;
                }
                return t1;
            }
        }
    }

    /**
     * 10进制转的16进制数据
     *
     * @param num 10进制的整数数据
     * @param length 数据位数,位数不够前补0
     */
    public static String get16(String num, int length) {
        long number = Long.parseLong(num);
        if (number < 0) {
            long c = number + 255;
            String s = ByteUtil.decimal2fitHex(c);
            if (s.length() == length) {
                return s;
            } else {
                String t = s;
                int i = length - t.length();
                for (int j = 0; j < i; j++) {
                    t = "0" + t;
                }
                return t;
            }
        } else {
            String s2 = ByteUtil.decimal2fitHex(number);
            if (s2.length() == length) {
                return s2;
            } else {
                String t1 = s2;
                int i = length - t1.length();
                for (int j = 0; j < i; j++) {
                    t1 = "0" + t1;
                }
                return t1;
            }
        }
    }

    /**
     * 10进制转换16进制
     *
     * @param data 10进制的整数数据
     * @param length 数据位数,位数不够前补0
     */
    public static String parse10Data(int data, int length) {
        String s1 = String.valueOf(data);
        String s = Integer.toHexString(Integer.parseInt(s1));
        if (s1.length() == length) {
            return s;
        } else {
            String t = s;
            int i = length - t.length();
            for (int j = 0; j < i; j++) {
                t = "0" + t;
            }
            return t;
        }
    }

    /**
     * 10进制转换16进制
     *
     * @param data 10进制的整数数据
     * @param length 数据位数,位数不够前补0
     */
    public static String parse10Data(String data, int length) {
        String s = Integer.toHexString(Integer.parseInt(data));
        if (data.length() == s.length()) {
            return s;
        } else {
            String t = s;
            int i = length - t.length();
            for (int j = 0; j < i; j++) {
                t = "0" + t;
            }
            return t;
        }
    }

    /**
     * 将10进制数值  转换成16进制字符串(已处理过负数的情况)
     */
    public static String parseTo16(long n) {
        if (n < 0) {
            String result = ByteUtil.decimal2fitHex(~Math.abs(n) + 1);
            int length = result.lastIndexOf("F");
            String temp = result.substring(length);
            if (temp.length() % 2 != 0) temp = "F" + temp;
            return temp;
        }

        return ByteUtil.decimal2fitHex(n);
    }

    /**
     * 将16进制字符串转为10 进制(已处理过负数的情况)
     *
     * @param hex 被转换的16进制字符串
     */
    public static long parseTo10(String hex) {
        int length = hex.length();
        hex = hex.toUpperCase();
        BigInteger b = new BigInteger(hex, 16);
        if (hex.startsWith("F")) {
            //负数
            String temp = parseTo16(~(b.intValue() - 1));
            if (temp.length() > length) temp = temp.substring(temp.length() - length);
            return -new BigInteger(temp, 16).intValue();
        } else {
            //正数
            return b.intValue();
        }
    }

    /**
     * 16进制转ASCII
     *
     * @param hex
     * @return
     */
    public static String hex2Str(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    /**
     * 十六进制转正负数
     * (2个字节的)
     */
    public static int parseHex4(String num) {
        if (num.length() != 4) {
            throw new NumberFormatException("Wrong length: " + num.length() + ", must be 4.");
        }
        int ret = Integer.parseInt(num, 16);
        ret = ((ret & 0x8000) > 0) ? (ret - 0x10000) : (ret);
        return ret;
    }

    /**
     * 转换二进制数值
     * @param value 要转换的数值
     * @param byteLen 该值占用的字节大小
     * @return
     */
    public static String toBinString(long value, int byteLen) {
        
        int bitLen = byteLen * 8;
        char[] chars = new char[bitLen];
        Arrays.fill(chars, '0');
        int charPos = bitLen;
        do {
            if ((value & 1) > 0) {
                chars[--charPos] = '1';
            }
            value >>>= 1;
        } while (value != 0 && charPos > 0);
        
        return new String(chars);
    }
}
