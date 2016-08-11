package com.akkafun.common.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by liubin on 2016/8/5.
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {


    /**
     * 截取字符串, 中文按2个字节长度计算
     * @param str
     * @param n
     * @return
     */
    public static String abbreviate4Unicode(String str, int n) {
        if (str == null)return "";
        if (n <= 0) return str;
        try {
            return cutString(str, n);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    private static String cutString(String s, int length) throws UnsupportedEncodingException {

        byte[] bytes = s.getBytes("Unicode");
        int n = 0; // 表示当前的字节数
        int i = 2; // 要截取的字节数，从第3个字节开始
        for (; i < bytes.length && n < length; i++){
            // 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
            if (i % 2 == 1){
                n++; // 在UCS2第二个字节时n加1
            }
            else{
                // 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
                if (bytes[i] != 0){
                    n++;
                }
            }
        }
        // 如果i为奇数时，处理成偶数
        if (i % 2 == 1){
            // 该UCS2字符是汉字时，去掉这个截一半的汉字
            if (bytes[i - 1] != 0){
                i = i - 1;
            }
            // 该UCS2字符是字母或数字，则保留该字符
            else{
                i = i + 1;
            }
        }

        String result = new String(bytes, 0, i, "Unicode");

        if(i < bytes.length && isNotBlank(result)) {
            result = result + "...";
        }

        return result;

    }
}
