package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailSender {
    private static final Properties config = new Properties();

    static {
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            config.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean send(String toEmail, String subject, String htmlContent) {
        final String username = config.getProperty("smtp.username");
        final String password = config.getProperty("smtp.password");
        final String host = config.getProperty("smtp.host");
        final String port = config.getProperty("smtp.port");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
}