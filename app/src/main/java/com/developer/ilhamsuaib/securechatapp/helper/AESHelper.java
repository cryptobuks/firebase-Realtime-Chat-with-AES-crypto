package com.developer.ilhamsuaib.securechatapp.helper;

/**
 * Created by ilham on 13/12/2016.
 */

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {

    public static String encrypt(String seed, String cleartext)
            throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());//untuk membuat raw key yang nantinya dibutuhkan saat proses enkripsi
        byte[] result = encrypt(rawKey, cleartext.getBytes());//menyimpan hasil enkripsi ke variabel result dalam bentuk byte
        return toHex(result);//byte data hasil enkripsi di jadikan hexa sebagai hasil enkripsi
    }

    public static String decrypt(String seed, String encrypted)
            throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);//data enkripsi (hexa) di konversi ke array byte
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        byte[] raw = Base64.decode(new String("n9dReP+BPwHWGCLpDQe+MQ==").getBytes(), 0);
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");//membuat secret key dari raw key untuk metode AES
        Cipher cipher = Cipher.getInstance("AES");//deklarasi metode yang digunakan
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);//inisilalisasi proses enkripsi
        byte[] encrypted = cipher.doFinal(clear);//mengenkripsi byte text
        return encrypted;//mengembalikan nilai dari hasil enkripsi dalam bentuk byte
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKey skeySpec = new SecretKeySpec(raw, "AES");//membuat secret key dari raw key untuk metode AES
        Cipher cipher = Cipher.getInstance("AES");//deklarasi metode yang digunakan
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);//inisilalisasi proses dekripsi
        byte[] decrypted = cipher.doFinal(encrypted);//dekripsi byte data dari chiper text
        return decrypted;//mengembalikan nilai hasil dekripsi dalam bentuk byte
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}
