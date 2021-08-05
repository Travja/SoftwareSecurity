package me.travja.md5;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

    public static void main(String[] args) {
        try {
            System.out.print("Please enter text to hash: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String input = in.readLine();
            String hashed = hashText(input);
            System.out.println("Hashed text: " + hashed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String hashText(String text) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(text.getBytes());

            for (byte b : digest.digest()) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not hash password using MD5");
            e.printStackTrace();
            return null;
        }
    }

}
