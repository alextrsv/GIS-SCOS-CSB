package gisscos.studentcard.services.Impl;

import gisscos.studentcard.repositories.IPermanentQRRepository;
import gisscos.studentcard.services.IPermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiceImpl implements IPermanentQRService {

    private final IPermanentQRRepository permanentQRRepository;

    @Autowired
    public PermanentQRServiceImpl(IPermanentQRRepository permanentQRRepository) {
        this.permanentQRRepository = permanentQRRepository;
    }

    @Override
    public ResponseEntity<Resource> downloadQRAsFile(String userToken) {
        /*
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
