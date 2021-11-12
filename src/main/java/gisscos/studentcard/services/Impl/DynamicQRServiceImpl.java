package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.OrganizationDTO;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.IDynamicQRRepository;
import gisscos.studentcard.services.IDynamicQRService;
import gisscos.studentcard.utils.mail.MailUtil;
import gisscos.studentcard.utils.mail.QRMessage;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicQRServiceImpl implements IDynamicQRService {


    private final IDynamicQRRepository dynamicQRRepository;

    private final VamRestClient vamRestClient;

    final GisScosApiRestClient gisScosApiRestClient;

    private final MailUtil mailUtil;

    public DynamicQRServiceImpl(IDynamicQRRepository dynamicQRRepository, MailUtil mailUtil, VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient) {
        this.dynamicQRRepository = dynamicQRRepository;
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
    public Optional<List<DynamicQR>> getQRByUserAndOrganization(UUID userId, String organizationId) {
        List<DynamicQR> usersQrs = dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId);
        if (!usersQrs.isEmpty())
            return Optional.ofNullable(dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId));
        else return Optional.empty();
    }

    @Override
    public Optional<Resource> downloadQRAsFile(UUID userId, String organizationId) {

      Optional<DynamicQR> activeQR = getActiveQRByOrganization(userId, organizationId);
        if (activeQR.isPresent()) {
            BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(activeQR.get().getContent());
            return Converter.getResource(qrCodeImage);
        }
        else return Optional.empty();
    }

    private Optional<DynamicQR> getActiveQRByOrganization(UUID userId, String organizationId){
        Optional<List<DynamicQR>> dynamicQRs = getQRByUserAndOrganization(userId, organizationId);

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
    public Optional<List<DynamicQR>> getAllPermittedQRsAsFile(UUID userId) {
        Optional<List<DynamicQR>> dynamicQRsByUser = Optional.ofNullable(dynamicQRRepository.getByUserId(userId));

        List<DynamicQR> activeQRs = new ArrayList<>();

        if (dynamicQRsByUser.isPresent()) {
            for (DynamicQR qr : dynamicQRsByUser.get()) {
                if (qr.getStatus() != QRStatus.EXPIRED && qr.getStatus() != QRStatus.DELETED)
                    activeQRs.add(qr);
            }
        }
        if (activeQRs.size() != 0)
            return Optional.of(activeQRs);
        else return Optional.empty();
    }


    @Override
    public ResponseEntity<Resource> sendQRViaEmail(UUID userId, String organizationId) {

        StudentDTO studentDTO = vamRestClient.makeGetStudentRequest(userId).get();
        studentDTO.setEmail("sasha2.tara2000@yandex.ru");

        OrganizationDTO organizationDTO = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);

        QRMessage qrMessage = new QRMessage(studentDTO, getActiveQRByOrganization(userId, organizationId).get(), organizationDTO );

        qrMessage.prepareMessage();

        mailUtil.sendQRImage(qrMessage);

        return null;
    }

    @Override
    public Optional<List<String>> getQRsContentByOrganization(String organizationId) {
        return Optional.of(dynamicQRRepository.getByUniversityId(organizationId).stream()
                .map(DynamicQR::getContent)
                .collect(Collectors.toList()));
    }
}
