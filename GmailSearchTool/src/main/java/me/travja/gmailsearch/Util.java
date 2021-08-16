package me.travja.gmailsearch;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Util {

    private static BufferedReader reader;
    private static List<String> trueResponses = Arrays.asList("yes", "y", "true", "t", "yup");
    private static List<String> falseResponses = Arrays.asList("no", "n", "false", "f", "nope");

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

    public static Boolean getBoolean(String prompt) {
        boolean valid;
        String input = "";
        do {
            input = getString(prompt).toLowerCase();
            valid = trueResponses.contains(input) || falseResponses.contains(input);
            if (!valid)
                System.out.println("Invalid input. Please try again.");
        } while (!valid);

        return trueResponses.contains(input);
    }

    public static String getInputFromNotepad(String filename) {
        StringBuilder body = new StringBuilder();

        File file = new File(filename);
        try {
            file.createNewFile();
            Process proc = Runtime.getRuntime().exec("notepad.exe " + file.getAbsolutePath());
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String input;
            while ((input = reader.readLine()) != null) {
                if (body.length() != 0)
                    body.append("\n");
                body.append(input);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not get file input.");
            e.printStackTrace();
        }

        file.delete();
        return body.toString();
    }

}
