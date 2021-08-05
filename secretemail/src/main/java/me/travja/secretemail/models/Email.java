package me.travja.secretemail.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.travja.secretemail.util.Encryption;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.stream.Collectors;

@NoArgsConstructor
public class Email {

    @Getter
    @Setter
    private String to, from, subject, body;

    @Setter
    private boolean encrypted, signed, vaildSignature;

    public Email(String to, String from, String subject, String body) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    public static Email from(Message msg, String owner) throws MessagingException, IOException {
        String from = msg.getFrom()[0].toString();

        if (from.contains("<"))
            from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));

        from = from.toLowerCase();

        String to = String.join(", ", Arrays.stream(msg.getRecipients(Message.RecipientType.TO))
                .map(a -> a.toString()).collect(Collectors.toList())).toLowerCase();
        String subject = msg.getSubject();

        Email email = new Email(to, from, subject, null);

        StringBuilder sb = new StringBuilder();
        Object contents = msg.getContent();
        if (contents instanceof MimeMultipart) {
            for (int j = 0; j < ((MimeMultipart) contents).getCount(); j++) {
                BodyPart body = ((MimeMultipart) contents).getBodyPart(j);
                if (body.getContentType().startsWith("TEXT/PLAIN")) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                    }

                    break;
                } else
                    continue;
            }
        }
        if (sb.length() > 0) {
            String body = sb.substring(0, sb.length() - 1);
            email.setBody(body);
            if (body.startsWith("$$enc$$")) {
                email.setEncrypted(true);
                String rawKey = body.substring(body.indexOf("$$enc$$") + 7, body.indexOf(":::"));

                File keyFile = new File("keys", owner + ".private");

                String aesKey = Encryption.keyDecrypt(rawKey, Encryption.getPrivateKey(keyFile));

                String text = body.split(":::")[1];
                String signature = "";
                if (text.contains("$$sig$$")) {
                    signature = text.substring(text.indexOf("$$sig$$"));
                    text = text.substring(0, text.indexOf("$$sig$$"));
                }

                String decrypted = Encryption.aesDecrypt(text, aesKey);
                email.setBody(decrypted + signature);
            }

            if (email.getBody().contains("$$sig$$")) {

                email.setSigned(true);

                String text = email.getBody().split("\\$\\$sig\\$\\$")[0];
                String signature = email.getBody().split("\\$\\$sig\\$\\$")[1];

                PublicKey pubKey = Encryption.getPublicKey(new File("keys", from + ".pub"));

                String hash = Encryption.keyDecrypt(signature, pubKey);
                String hashedBody = Encryption.sha(text);

                email.setBody(text);
                email.setVaildSignature(hash.equals(hashedBody));

            }
        } else {
            email.setBody(" [ NO TEXT BODY ] ");
        }
        return email;
    }

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

        StringBuilder sb = new StringBuilder();

        sb.append("To: ").append(to)
                .append("\nFrom: ").append(from)
                .append("\nSubject: ").append(subject)
                .append("\n").append(txt).append("\n")
                .append(encrypted ? "--Encrypted--\t" : "")
                .append(signed ? "--Signed--" : "");
        if (signed)
            sb.append(vaildSignature ? "Valid--" : "Invalid Signature--");

        return sb.toString();
    }
}
