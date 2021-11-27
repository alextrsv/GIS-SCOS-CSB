package ru.edu.online.utils.mail;

import javax.mail.Session;
import java.util.Properties;

public class SessionFactory {
    private static Session session;

    public static Session getSession() {
        if (session != null) return session;
        try {
            Properties p = new Properties();
            p.put("mail.smtp.host", "192.168.31.245");
            p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.port", 25);
            SMTPAuthenticator smtpAuthenticator = new SMTPAuthenticator();
            session = Session.getDefaultInstance(p, smtpAuthenticator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }
}