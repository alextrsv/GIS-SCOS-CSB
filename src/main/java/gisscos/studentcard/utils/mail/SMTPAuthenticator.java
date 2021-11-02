package gisscos.studentcard.utils.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {
    public SMTPAuthenticator(){
        super();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String username = System.getenv("EMAIL_ADDRESS");
        String password = System.getenv("EMAIL_PASSWORD");
        return new PasswordAuthentication(username, password);

    }
}
