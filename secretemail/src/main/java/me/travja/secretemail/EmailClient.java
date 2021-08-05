package me.travja.secretemail;

import com.sun.mail.imap.IMAPFolder;
import lombok.Getter;
import me.travja.secretemail.models.Email;
import me.travja.secretemail.models.Menu;
import me.travja.secretemail.models.Option;
import me.travja.secretemail.util.Encryption;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static me.travja.secretemail.util.Util.getInt;
import static me.travja.secretemail.util.Util.getString;

public class EmailClient {

    @Getter
    private static EmailClient client;
    private static Session imapSession = null;
    private static Session smtpSession = null;
    private int imapPort = 993, smtpPort = 465;
    private String email = "",
            password = "",
            imapHost = "imap.gmail.com",
            smtpHost = "smtp.gmail.com";

    private Store store;

    private Menu menu;

    private IMAPFolder inbox;

    public static void run() {
        if (client == null) client = new EmailClient();
        client.start();
    }

    public void start() {
        try {
            setupFiles();
            initMenu();
            setupInbox();

            boolean again;
            do {
                Optional<Option> option = menu.present();
                again = option.isPresent();
                option.ifPresent(o -> o.getAction().run());
            } while (again);

        } catch (MessagingException e) {
            System.err.println("Could set up inbox: " + e.getMessage());
            e.printStackTrace();
        }

//        System.out.println("\n\n\n\n");
//        String encrypted = Encryption.keyEncrypt("This is some data.", Encryption.getPrivateKey(new File("keys", email + ".private")));
//        System.out.println(encrypted);
//        String decrypted = Encryption.keyDecrypt(encrypted, Encryption.getPublicKey(new File("keys", email + ".pub")));
//        System.out.println(decrypted);
    }

    private void initMenu() {
        menu = new Menu();
        menu.addOption(new Option("View Emails", () -> {
                    try {
                        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                        System.out.println("You have " + messages.length + " unread email" + (messages.length != 1 ? "s" : ""));
                        for (int i = messages.length - 1; i >= 0; i--) {
                            Message msg = messages[i];
                            Email email = readEmail(msg);
                            System.out.println("\n\n -----=[ Email #" + (i + 1) + " ]=----- ");
                            System.out.println(email);
                            System.out.println("\n\n");
                            if (i > 0) {
                                System.out.println("Press enter to view next email or type 'exit' to exit.");
                                String input = getString("> ");
                                if (input.equalsIgnoreCase("exit")) break;
                            }
                            msg.setFlag(Flags.Flag.SEEN, true);
                        }
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                    }
                }))
                .addOption(new Option("Send Emails", () -> {
                    String to = getString("To: ");
                    String subject = getString("Subject: ");
                    StringBuilder body = new StringBuilder();
                    System.out.println("Please compose the email body. Save the file when finished.");

                    File file = new File("email.txt");
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


                        System.out.println("\nComposition complete.\n");
                        System.out.println("Body is: " + body);
                    } catch (IOException e) {
                        System.err.println("Could not get file input.");
                        e.printStackTrace();
                    }
                    //TODO Ask if we want to encrypt/sign.

                    boolean sent = sendEmail(to, subject, body.toString());
                    if (sent)
                        System.out.println("Message sent.");
                    else
                        System.out.println("Message failed to send.");

                }))
                .addOption(new Option("Refresh inbox", () -> {
                    try {
                        countUnread();
                    } catch (MessagingException e) {
                        System.err.println("Could not read inbox.");
                        e.printStackTrace();
                    }
                }));
    }

    private void setupFiles() {
        email = getString("Please enter the email you would like to use: ");

        new File("keys").mkdir();
        if (!new File("keys", email + ".private").exists())
            Encryption.generateKeyPair(email);

        if (!new File("conf", email + ".conf").exists()) {
            System.out.println("No configuration found for that email. Please configure it now.\n");
            setupMailServer();
        } else
            loadConfiguration(email);
    }

    public void setupInbox() throws MessagingException {
        System.out.println("Connecting to your inbox...");
        Session session = this.getImapSession();
        store = session.getStore();
        store.connect(imapHost, imapPort, email, password);
        countUnread();
    }

    private void countUnread() throws MessagingException {
        inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        System.out.println("You have " + messages.length + " unread email" + (messages.length != 1 ? "s" : ""));
    }

    private Email readEmail(Message message) throws MessagingException, IOException {
        Message msg = message;
        String from = msg.getFrom()[0].toString();
        String to = String.join(", ", Arrays.stream(msg.getRecipients(Message.RecipientType.TO))
                .map(a -> a.toString()).collect(Collectors.toList()));
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
            //TODO Determine if this is encrypted/signed and decrypt appropriately.
            email.setBody(body);
        } else {
            email.setBody(" [ NO TEXT BODY ] ");
        }
        return email;
    }

    public boolean sendEmail(String to, String subject, String body) {
        try {
            Session session = getSmtpSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, "text/plain");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setupMailServer() {
        System.out.println("Please configure your IMAP/SMTP information for your mail client.");
        System.out.println("These mail settings assume you are using TLS/SSL. Enter accordingly.");
        password = getString("Enter password: ");

        imapHost = getString("Enter the IMAP host: ");
        imapPort = getInt("Enter IMAP port: ");

        smtpHost = getString("Enter the SMTP host: ");
        smtpPort = getInt("Enter SMTP port: ");
        saveConfiguration();
    }

    public void loadConfiguration(String email) {
        File confFile = new File("conf", email + ".conf");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(confFile)))) {
            this.email = reader.readLine();
            this.password = Encryption.keyDecrypt(reader.readLine(), Encryption.getPrivateKey(new File("keys", email + ".private")));
            this.imapHost = reader.readLine();
            this.imapPort = Integer.parseInt(reader.readLine());
            this.smtpHost = reader.readLine();
            this.smtpPort = Integer.parseInt(reader.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration() {
        new File("conf").mkdir();
        File confFile = new File("conf", email + ".conf");
        try (FileWriter out = new FileWriter(confFile)) {
            out.write(email + "\n");
            //Encrypt password with public key so it can only be decrypted with private key
            out.write(Encryption.keyEncrypt(password, Encryption.getPublicKey(new File("keys", email + ".pub"))) + "\n");
            out.write(imapHost + "\n");
            out.write(imapPort + "\n");
            out.write(smtpHost + "\n");
            out.write(smtpPort + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Session getImapSession() {
        try {
            if (imapSession != null && imapSession.getStore().isConnected())
                return imapSession;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.host", imapHost);
        props.setProperty("mail.imap.port", String.valueOf(imapPort));
        props.setProperty("mail.imap.ssl.trust", "true");
        props.setProperty("mail.imap.starttls.enable", "true");
//        SSLSocket sock = null;
//        sock.setEnabledProtocols

        imapSession = Session.getDefaultInstance(props, null);

        return imapSession;
    }

    private Session getSmtpSession() {
        if (smtpSession != null)
            return smtpSession;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.host", smtpHost);
        props.setProperty("mail.smtp.port", String.valueOf(smtpPort));

        smtpSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        return smtpSession;
    }

}