package gisscos.studentcard.utils.mail;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.services.Impl.Converter;
import gisscos.studentcard.services.Impl.QrGenerator;

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

    private final StudentDTO studentDTO;
    private final DynamicQR dynamicQR;
    private final OrganizationDTO organizationDTO;


    public QRMessage(StudentDTO studentDTO, DynamicQR dynamicQR, OrganizationDTO organizationDTO){
        super(SessionFactory.getSession());
        this.studentDTO = studentDTO;
        this.dynamicQR = dynamicQR;
        this.organizationDTO = organizationDTO;
    }

    public void prepareMessage(){
        try {
            Multipart multiContent = new MimeMultipart();

            this.setRecipient(Message.RecipientType.TO, new InternetAddress(studentDTO.getEmail()));
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

        this.setSubject("Your pass QR-code");
        textBodyPart.setText("Hello, " + studentDTO.getName()
                + "!\n Here your personal QR-code for organization: "
                + organizationDTO.getFull_name());
        return textBodyPart;
    }

}
