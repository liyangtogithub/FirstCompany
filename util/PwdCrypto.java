package com.desay.iwan2.util;

import java.security.MessageDigest;

public class PwdCrypto {
    public static String encrypt(String password) throws Exception {
        if (password == null || "".equals(password))
            return null;
        byte[] buf = password.getBytes();
        MessageDigest algorithm = null;
//		try {

        algorithm = MessageDigest.getInstance("MD5");
        algorithm.reset();
        algorithm.update(buf);
        byte[] digest1 = algorithm.digest();
        int[] digest1_int = new int[digest1.length];
        for (int i = 0; i < digest1.length; i++)
            digest1_int[i] = (int) (digest1[i] & 0xFF);
        StringBuffer SB1 = new StringBuffer("");
        for (int i = 0; i < digest1_int.length; i++)
            SB1.append(digest1_int[i]);
        return SB1.toString();
//		} catch (Exception e) {
//            e.printStackTrace();
//		}
//		return null;
    }
}
