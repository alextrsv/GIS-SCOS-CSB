package gisscos.studentcard.utils.mail;

import javax.mail.Session;
import java.util.Properties;

public class SessionFactory {
    private static Session session;

    public static Session getSession() {
        if (session != null) return session;
        try {
            Properties p = new Properties();
            p.put("mail.smtp.host", "smtp.yandex.ru");
            p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.port", 465);
            SMTPAuthenticator smtpAuthenticator = new SMTPAuthenticator();
            session = Session.getDefaultInstance(p, smtpAuthenticator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }
}