package gisscos.studentcard.utils.mail;


import org.springframework.stereotype.Component;

import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class MailUtil {

      String fromAdr = "sasha.tara2000@yandex.ru";


    public void sendQRImage(MimeMessage message) {

        try {
            message.setFrom(new InternetAddress(fromAdr));
            System.out.println("Now sending...");
            Transport.send(message);

        } catch (Exception ex) {
            System.err.println("ОШИБОЧКА");
            ex.printStackTrace();
        }

    }
}
