package com.akkafun.user.api.utils;

import java.util.regex.Pattern;

public class RegExpUtils {

    public static final String USERNAME_REG_EXP = "^[A-Za-z0-9_\\-\\u4e00-\\u9fa5]{2,20}$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REG_EXP);

    public static final String PHONE_REG_EXP = "^[1][\\d]{10}";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REG_EXP);

    public static final String EMAIL_REG_EXP = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    private static Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REG_EXP);


    public static boolean isPhone(String str) {
        return str != null && PHONE_PATTERN.matcher(str).matches();

    }

    public static boolean isEmail(String str) {
        return str != null && EMAIL_PATTERN.matcher(str).matches();

    }

    public static boolean isUsername(String str) {
        return str != null && USERNAME_PATTERN.matcher(str).matches();

    }


    public static void main(String[] args) {
        String phone1 = "18622223333";
        String email1 = "123@1.com";
        String email2 = "123@.com";

        System.out.println(isPhone(phone1));
        System.out.println(isEmail(email1));
        System.out.println(isEmail(email2));

    }

}
