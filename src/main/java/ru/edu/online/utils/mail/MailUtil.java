package ru.edu.online.utils.mail;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class MailUtil {

    @Value("${email.sender.address}")
    String fromAdr;

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
