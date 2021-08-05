package me.travja.secretemail.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Encryption {

    public static String keyEncrypt(String data, Key key) {
        String encoded = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(bytes);

            encoded = Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encoded;
    }

    public static String keyDecrypt(String data, Key key) {
        String decoded = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = Base64.getDecoder().decode(data);
            byte[] decrypted = cipher.doFinal(bytes);

            decoded = new String(decrypted, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return decoded;
    }

    public static String sign(String data, Key key) {
        String hashed = sha(data);
        String encrypted = keyEncrypt(hashed, key);

        return encrypted;
    }

    public static String aesEncrypt(String data, String password) {
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

    public static String aesDecrypt(String data, String password) {
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
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            bytes = sha.digest(bytes);

            String base64 = Base64.getEncoder().encodeToString(bytes);
            return base64;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PublicKey getPublicKey(File file) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(Files.readAllBytes(file.toPath())));
            return factory.generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey(File file) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Files.readAllBytes(file.toPath())));
            return factory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getPublicKey(String base64Key) {
        PublicKey pubKey = null;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Key.getBytes()));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            pubKey = factory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error getting public key");
            e.printStackTrace();
        }
        return pubKey;
    }

    public static PrivateKey getPrivateKey(String base64Key) {
        PrivateKey privKey = null;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64Key.getBytes()));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            privKey = factory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privKey;
    }

    public static void generateKeyPair(String id) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            PrivateKey privKey = pair.getPrivate();
            PublicKey pubKey = pair.getPublic();

            try (FileOutputStream fos = new FileOutputStream(new File("keys", id + ".pub"))) {
                fos.write(Base64.getEncoder().encode(pubKey.getEncoded()));
            }

            try (FileOutputStream fos = new FileOutputStream(new File("keys", id + ".private"))) {
                fos.write(Base64.getEncoder().
                        encode(privKey.getEncoded()));
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
