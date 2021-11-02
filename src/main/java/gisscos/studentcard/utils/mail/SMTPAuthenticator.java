package buisnesslogic.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {
    public SMTPAuthenticator(){
        super();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String username = "sasha.tara2000@yandex.ru";
        String password = "Sasha1335";
        if ((username != null) && (username.length() > 0) && (password != null)
                && (password.length   () > 0)) {
            return new PasswordAuthentication(username, password);
        }

        return null;
    }
}
