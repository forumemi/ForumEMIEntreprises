package com.forum.emi.app;

public class Crypto {
    public static String key = "fee25:";

    public static String encrypt(String str) {
        String cryptedStr = key + str;
        return cryptedStr;
    }
    public static String decrypt(String str){
        String decryptedStr = str.substring(key.length());
        return decryptedStr;
    }
}

