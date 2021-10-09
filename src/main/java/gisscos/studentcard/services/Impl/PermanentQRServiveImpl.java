package gisscos.studentcard.services.Impl;

import gisscos.studentcard.repositories.PermanentQRRepository;
import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiveImpl implements PermanentQRService {

    private final PermanentQRRepository permanentQRRepository;

    @Autowired
    public PermanentQRServiveImpl(PermanentQRRepository permanentQRRepository) {
        this.permanentQRRepository = permanentQRRepository;
    }

    @Override
    public ResponseEntity<Resource> downloadQRAsFile(String userToken) {
        /**
         *  @makeInfoString() - mock для получения информативной строки.
         *  В дальнешим, инфа будет браться из данных о пользователе на ГИС СЦОС
         */
        String content = makeInfoString(userToken);

        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return QRImageAsResource.getResourceResponseEntity(qrCodeImage);
    }


    private String makeInfoString(String userToken){
        return "Some info about User founded by usrToken, " + userToken;

    }


}
