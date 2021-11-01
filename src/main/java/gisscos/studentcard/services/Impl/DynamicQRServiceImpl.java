package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.DynamicQRDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.DynamicQRRepository;
import gisscos.studentcard.repositories.UserRepository;
import gisscos.studentcard.services.DynamicQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicQRServiceImpl implements DynamicQRService {

    @Autowired
    DynamicQRRepository dynamicQRRepository;

    @Autowired
    UserRepository userRepository;


    /**
     * Метод получения информации о динамическом QR-коде
     * @param userId - токен пользователя
     * @return DynamicQR
     * todo: в качестве контента сейчас применяется строка "someContent". Нужно заменить на реальные данные (от ГИС СЦОС)
     * */
    @Override
    public Optional<List<DynamicQR>> getInfo(UUID userId, UUID organizationId) {
        List<DynamicQR> usersQrs = dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId);
        if (!usersQrs.isEmpty())
            return Optional.ofNullable(dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId));
        else return Optional.empty();

    }


    @Override
    public ResponseEntity<Resource> downloadQRAsFile(UUID userId, UUID organizationId) {

        Optional<List<DynamicQR>> dynamicQRs = getInfo(userId, organizationId);

        Optional<DynamicQR> activeQR = Optional.empty();
        if (dynamicQRs.isPresent()) {
            for (DynamicQR qr : dynamicQRs.get()) {
                if (qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                    activeQR = Optional.of(qr);
            }
        }
//        dynamicQRs.ifPresent(dynamicQRS -> dynamicQRS.stream()
//                .filter(dynamicQR -> dynamicQR.getStatus() != QRStatus.EXPIRED)
//                .filter(dynamicQR -> dynamicQR.getStatus() != QRStatus.DELETED)
//                .collect(Collectors.toList()));
        if (activeQR.isPresent()) {
            BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(activeQR.get().getContent());
            return QRImageAsResource.getResourceResponseEntity(qrCodeImage);
        }
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

}
