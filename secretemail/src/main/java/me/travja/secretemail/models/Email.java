package me.travja.secretemail.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class Email {

    @Getter
    @Setter
    private String to, from, subject, body;

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder();
        String[] split = body.split("\n");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            txt.append("| ").append(s);
            if (i + 1 < split.length)
                txt.append("\n");
        }

        return "To: " + to +
                "\nFrom: " + from +
                "\nSubject: " + subject +
                "\n" + txt;
    }
}
