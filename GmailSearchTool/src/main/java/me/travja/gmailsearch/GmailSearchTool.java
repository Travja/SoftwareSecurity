package me.travja.gmailsearch;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class GmailSearchTool {
    private static final String      APPLICATION_NAME      = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY          = GsonFactory.getDefaultInstance();
    private static final String      TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_READONLY
    );

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String label) throws IOException {
        // Load client secrets.
        InputStream in = GmailSearchTool.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        try {
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize(label);
        } catch (IOException e) {
            System.err.println("No authorization granted. Please grant permission to an email account.");
            Util.getString("Press enter to continue.");
            return null;
        }
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String     id   = Util.getString("Enter account id: ");
        String     user = "me";
        Credential credentials;
        do {
            credentials = getCredentials(HTTP_TRANSPORT, id);
        } while (credentials == null);

        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String search = Util.getString("Enter a search string: ");

        ListMessagesResponse listResponse = service.users().messages().list(user).setQ(search).execute();
        List<Message>        messages     = listResponse.getMessages();

        if (messages == null || messages.isEmpty()) {
            System.out.println("No messages found.");
        } else {
            System.out.printf("%d messages match your query.\n\n", messages.size());
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                System.out.printf(" -----=[ Message #%d ]=----- \n", i + 1);
                Message msg = service.users().messages().get(user, message.getId()).execute();
                if (msg.getPayload() != null) {
                    List<MessagePartHeader> headers = msg.getPayload().getHeaders();
                    String                  subject = "";
                    String                  from    = "";
                    for (MessagePartHeader header : headers) {
                        if (header.getName().equals("Subject"))
                            subject = header.getValue();
                        else if (header.getName().equals("From"))
                            from = header.getValue();
                    }
                    System.out.printf("Subject: %s\nFrom: %s\n", subject, from);
                    String body = "";
                    for (MessagePart part : msg.getPayload().getParts()) {
                        if (part.getMimeType().startsWith("text/plain"))
                            body += new String(part.getBody().decodeData());
                    }
                    System.out.println(body);
                } else {
                    System.out.println("\t! No payload");
                }
                System.out.println(" -------------------------- \n\n");
            }
        }
    }
}