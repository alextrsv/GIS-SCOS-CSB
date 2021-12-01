package ru.edu.online.utils.mail;

import ru.edu.online.entities.DynamicQR;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.services.Impl.Converter;
import ru.edu.online.services.Impl.QrGenerator;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class QRMessage extends MimeMessage {

    private  StudentDTO studentDTO;
    private  UserDTO userDTO;
    private final DynamicQR dynamicQR;
    private final OrganizationDTO organizationDTO;


    public QRMessage(StudentDTO studentDTO, DynamicQR dynamicQR, OrganizationDTO organizationDTO){
        super(SessionFactory.getSession());
        this.studentDTO = studentDTO;
        this.dynamicQR = dynamicQR;
        this.organizationDTO = organizationDTO;
        prepareMessage(studentDTO.getEmail());
    }
    public QRMessage(UserDTO userDTO, DynamicQR dynamicQR, OrganizationDTO organizationDTO){
        super(SessionFactory.getSession());
        this.userDTO = userDTO;
        this.dynamicQR = dynamicQR;
        this.organizationDTO = organizationDTO;
        prepareMessage(userDTO.getEmail());
    }


    public void prepareMessage(String email){
        try {
            Multipart multiContent = new MimeMultipart();

            this.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            multiContent.addBodyPart(getTextBodyPart());
            multiContent.addBodyPart(getImageBodyPart());

            this.setContent(multiContent);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }


    private MimeBodyPart getImageBodyPart() throws MessagingException, IOException {
        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(dynamicQR.getContent());

        MimeBodyPart qrImageAttachment = new MimeBodyPart();
        ByteArrayDataSource bds = new ByteArrayDataSource(Converter.convertToBytes(qrCodeImage), "image/png");
        qrImageAttachment.setDataHandler(new DataHandler(bds));
        qrImageAttachment.setFileName(bds.getName());

        return qrImageAttachment;
    }

    private MimeBodyPart getTextBodyPart() throws MessagingException {
        MimeBodyPart textBodyPart = new MimeBodyPart();

        this.setSubject("Ваш QR-код для прохода в организацию");
        textBodyPart.setText("Здравствуйте, " + studentDTO.getName()
                + "!\n Ваш персональный QR-код для прохода в организацию "
                + organizationDTO.getShort_name());
        return textBodyPart;
    }

}
