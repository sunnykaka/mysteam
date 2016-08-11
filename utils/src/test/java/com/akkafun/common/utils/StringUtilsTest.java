package com.akkafun.common.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by liubin on 2016/8/5.
 */
public class StringUtilsTest {

    @Test
    public void test() {

        assertResult("你叫什么名123字啊", 1, "");
        assertResult("你叫什么名123字啊", 2, "你...");
        assertResult("你叫什么名123字啊", 3, "你...");
        assertResult("你叫什么名123字啊", 4, "你叫...");
        assertResult("你叫什么名123字啊", 5, "你叫...");
        assertResult("你叫什么名123字啊", 16, "你叫什么名123字...");
        assertResult("你叫什么名123字啊", 17, "你叫什么名123字啊");
        assertResult("你叫什么名123字啊", 18, "你叫什么名123字啊");
        assertResult("你叫什么名123字啊", 88, "你叫什么名123字啊");

        assertResult("abcdef", 1, "a...");
        assertResult("abcdef", 2, "ab...");
        assertResult("abcdef", 5, "abcde...");
        assertResult("abcdef", 6, "abcdef");
        assertResult("abcdef", 7, "abcdef");

        assertResult("你123叫什么名字啊", 3, "你1...");
        assertResult("你123叫什么名字啊", 4, "你12...");
        assertResult("你123叫什么名字啊", 5, "你123...");
        assertResult("你123叫什么名字啊", 6, "你123...");
        assertResult("你123叫什么名字啊", 7, "你123叫...");

    }


    private static void assertResult(String str, int n, String result) {

        assertThat(StringUtils.abbreviate4Unicode(str, n), is(result));
    }


}
