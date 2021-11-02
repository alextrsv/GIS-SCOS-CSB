package gisscos.studentcard.utils.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {
    public SMTPAuthenticator(){
        super();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String username = "*****";
        String password = "*****";
        return new PasswordAuthentication(username, password);

    }
}
