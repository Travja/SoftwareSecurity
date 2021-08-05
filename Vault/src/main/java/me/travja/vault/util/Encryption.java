package me.travja.vault.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Encryption {

    public static String encrypt(String data, String password) {
        try {
            SecretKeySpec key = convertPasswordToKey(password);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Encountered an error during encryption: " + e.toString());
        }

        return null;
    }

    public static String decrypt(String data, String password) {
        try {
            SecretKeySpec key = convertPasswordToKey(password);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            System.out.println("Encountered an error during encryption: " + e.toString());
        }

        return null;
    }

    public static SecretKeySpec convertPasswordToKey(String password) {
        try {
            //Convert password to bytes, sha hash, and get first 16 bytes.
            byte[] bytes = password.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            bytes = sha.digest(bytes);
            bytes = Arrays.copyOf(bytes, 16);

            return new SecretKeySpec(bytes, "AES");
        } catch (Exception e) {
            System.out.println("Error converting password to key: " + e.toString());
            e.printStackTrace();
        }

        return null;
    }

    public static String sha(String password) {
        try {
            byte[] bytes = password.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            bytes = sha.digest(bytes);

            String base64 = Base64.getEncoder().encodeToString(bytes);
            return base64;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
