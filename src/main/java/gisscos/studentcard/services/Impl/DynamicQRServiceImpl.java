package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.DynamicQRRepository;
import gisscos.studentcard.repositories.UserRepository;
import gisscos.studentcard.services.DynamicQRService;
import gisscos.studentcard.utils.mail.MailUtil;
import gisscos.studentcard.utils.mail.QRMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DynamicQRServiceImpl implements DynamicQRService {

    final DynamicQRRepository dynamicQRRepository;

    final UserRepository userRepository;

    private final VamRestClient vamRestClient;

    final GisScosApiRestClient gisScosApiRestClient;

    private final MailUtil mailUtil;

    public DynamicQRServiceImpl(DynamicQRRepository dynamicQRRepository, UserRepository userRepository, MailUtil mailUtil, VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient) {
        this.dynamicQRRepository = dynamicQRRepository;
        this.userRepository = userRepository;
        this.mailUtil = mailUtil;
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
    }


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
    public Optional<Resource> downloadQRAsFile(UUID userId, UUID organizationId) {

      Optional<DynamicQR> activeQR = getActiveQR(userId, organizationId);
        if (activeQR.isPresent()) {
            BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(activeQR.get().getContent());
            return Converter.getResource(qrCodeImage);
        }
        else return Optional.empty();
    }

    private Optional<DynamicQR> getActiveQR(UUID userId, UUID organizationId){
        Optional<List<DynamicQR>> dynamicQRs = getInfo(userId, organizationId);

        Optional<DynamicQR> activeQR = Optional.empty();
        if (dynamicQRs.isPresent()) {
            for (DynamicQR qr : dynamicQRs.get()) {
                if (qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                    activeQR = Optional.of(qr);
            }
        }
        return activeQR;
    }

    @Override
    public ResponseEntity<Resource> sendQRViaEmail(UUID userId, UUID organizationId) {

        StudentDTO studentDTO = vamRestClient.makeGetStudentRequest(userId);
        studentDTO.setEmail("*****");

        OrganizationDTO organizationDTO = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);

        QRMessage qrMessage = new QRMessage(studentDTO, getActiveQR(userId, organizationId).get(), organizationDTO );

        qrMessage.prepareMessage();

        mailUtil.sendQRImage(qrMessage);

        return null;
    }
}
