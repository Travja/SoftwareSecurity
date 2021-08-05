package me.travja.secretemail.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    private static BufferedReader reader;

    public static String getString(String prompt) {
        if (reader == null)
            reader = new BufferedReader(new InputStreamReader(System.in));

        if (prompt != null)
            System.out.print(prompt);
        String input = null;
        try {
            input = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return input;
    }

    public static int getInt(String prompt) {
        int num;
        do {
            try {
                num = Integer.parseInt(getString(prompt));
            } catch (NumberFormatException e) {
                num = -1;
                System.out.println("Invalid input. Please enter a number.");
            }
        } while (num == -1);

        return num;
    }

}
