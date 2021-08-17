package me.travja.gmailsearch;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private String subject, from, body;

    public static Email from(Message message) {
        try {
            Email   email = new Email();
            Message msg   = GmailSearchTool.gmailService.users().messages().get("me", message.getId()).execute();
            if (msg.getPayload() != null) {
                List<MessagePartHeader> headers = msg.getPayload().getHeaders();

                for (MessagePartHeader header : headers) {
                    if (header.getName().equals("Subject"))
                        email.setSubject(header.getValue());
                    else if (header.getName().equals("From"))
                        email.setFrom(header.getValue());
                }

                StringBuilder body = new StringBuilder();
                for (MessagePart part : msg.getPayload().getParts()) {
                    if (part.getMimeType().startsWith("text/plain"))
                        body.append(new String(part.getBody().decodeData()));
                }
                email.setBody(body.toString());
                return email;
            }
        } catch (IOException e) {
            System.err.println("Could not parse message.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Simply takes the search parameters and filters down the text appropriately
     *
     * @param search The keywords to filter
     */
    public void filterBy(String search) {
        if (search.trim().isEmpty())
            return;

        String keywords = String.join("|", search.split(" "));
        Pattern regex = Pattern.compile("(\\b[^\\s]*\\.?[\\s]){0,3}\\b(" + keywords + ")\\b([\\s]\\b[^\\s]*\\.?){0,3}",
                Pattern.CASE_INSENSITIVE);
        Matcher       mat       = regex.matcher(getBody().replaceAll("\r?\n", " "));
        StringBuilder fin       = new StringBuilder();
        int           lastGroup = 0;
        while (mat.find()) {
            lastGroup = mat.end();
            String match    = mat.group();
            String compiled = "";
            if (mat.start() != 0)
                compiled += "...";
            compiled += match;
            fin.append(compiled);
            if ((fin.indexOf("\n") == -1 && fin.length() > 100) ||
                    (fin.indexOf("\n") != -1 && fin.substring(fin.lastIndexOf("\n")).length() > 100))
                fin.append("\n");
        }

        if (mat.hitEnd() && lastGroup != mat.regionEnd())
            fin.append("...");

        if (fin.length() > 255)
            setBody(fin.substring(0, 255) + "...");
        else
            setBody(fin.toString());
    }

    @Override
    public String toString() {
        return "Subject: " + subject +
                "\nFrom: " + from +
                "\n" + body;
    }
}
