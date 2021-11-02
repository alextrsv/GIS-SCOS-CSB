package gisscos.studentcard.utils.mail;


import javax.mail.*;
import javax.mail.Message.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSender {
    static String toAdr;
    static String messageText;
    static private String fromAdr = "sasha.tara2000@yandex.ru";

    public MailSender() {}


    public static void makeSend(UserMessage userMessage) {
        toAdr = userMessage.getToAddr();
        messageText = userMessage.getMessageText();
        send();
    }

    public static void send()  {
        Properties p = new Properties();
        p.put("mail.smtp.host", "smtp.yandex.ru");
        p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.port", 465);
        SMTPAuthenticator smtpAuthenticator = new SMTPAuthenticator();
        Session s = Session.getDefaultInstance(p, smtpAuthenticator);

        try {
            Message mess = new MimeMessage(s);
            mess.setFrom(new InternetAddress(fromAdr));
            mess.setRecipient(RecipientType.TO, new InternetAddress(toAdr));
            mess.setSubject(messageText);
            mess.setText(messageText);
            System.out.println("Now sending...");
            Transport.send(mess);

        } catch (Exception ex) {
            System.err.println("ОШИБОЧКА");
            ex.printStackTrace();
        }

    }
}
