package ru.edu.online.services.Impl;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.DynamicQR;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.enums.QRStatus;
import ru.edu.online.repositories.IDynamicQRRepository;
import ru.edu.online.services.IDynamicQRService;
import ru.edu.online.utils.mail.MailUtil;
import ru.edu.online.utils.mail.QRMessage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
     * */
    @Override
    public Optional<List<DynamicQR>> getQRByUserAndOrganization(String userId, String organizationId) {
            List<DynamicQR> usersQrs = dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId);
        if (!usersQrs.isEmpty())
            return Optional.ofNullable(dynamicQRRepository.getByUserIdAndUniversityId(userId, organizationId));
        else return Optional.empty();
    }

    @Override
    public Optional<Resource> downloadQRAsFile(String userId, String organizationId) {

      Optional<DynamicQR> activeQR = getActiveQRByOrganization(userId, organizationId);
        if (activeQR.isPresent()) {
            BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(activeQR.get().getContent());
            return Converter.getResource(qrCodeImage);
        }
        else return Optional.empty();
    }

    private Optional<DynamicQR> getActiveQRByOrganization(String userId, String organizationId){
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
    public Optional<List<DynamicQR>> getAllPermittedQRsAsFile(String userId) {
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
    public Optional<Integer> sendQRViaEmail(String userId, String organizationId) {

        Optional<UserDTO> scosUser = gisScosApiRestClient.makeGetUserRequest(userId);
        if (scosUser.isEmpty() || scosUser.get().getEmail() == null) return Optional.of(-1);

        Optional<StudentDTO> studentDTO = vamRestClient.makeGetStudentByEmailRequest(scosUser.get().getEmail());

        if (studentDTO.isPresent()) {
            studentDTO.get().setScos_id(scosUser.get().getUser_id());
            sendToStudent(studentDTO.get(), organizationId);
        }
        else sendToUser(scosUser.get(), organizationId);

        return Optional.of(1);
    }


    private Integer sendToStudent(StudentDTO studentDTO, String organizationId){
//        studentDTO.setEmail("sasha2.tara2000@yandex.ru"); // пока что

        Optional<OrganizationDTO> organizationDTO;
        if (organizationId.equals("2a0fc6e23c7744478ab6114add556f3e_535158318") || organizationId.equals("1027806868154"))
            organizationDTO = gisScosApiRestClient.makeGetOrganizationByOrgnRequest(organizationId);
        else organizationDTO = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);
        if (organizationDTO.isEmpty()) return -1;

        Optional<DynamicQR> qr = getActiveQRByOrganization(studentDTO.getScos_id(), organizationId);
        if (qr.isEmpty()) return -1;

        QRMessage qrMessage = new QRMessage(studentDTO, qr.get(), organizationDTO.get());

        mailUtil.sendQRImage(qrMessage);

        return 1;
    }

    private Integer sendToUser(UserDTO userDTO, String organizationId){
//        userDTO.setEmail("sasha2.tara2000@yandex.ru"); // пока что


        Optional<OrganizationDTO> organizationDTO;
        if (organizationId.equals("2a0fc6e23c7744478ab6114add556f3e_535158318") || organizationId.equals("1027806868154"))
            organizationDTO = gisScosApiRestClient.makeGetOrganizationByOrgnRequest(organizationId);
        else organizationDTO = gisScosApiRestClient.makeGetOrganizationRequest(organizationId);
        if (organizationDTO.isEmpty()) return -1;

        Optional<DynamicQR> qr = getActiveQRByOrganization(userDTO.getUser_id(), organizationId);
        if (qr.isEmpty()) return -1;

        QRMessage qrMessage = new QRMessage(userDTO, qr.get(), organizationDTO.get());

        mailUtil.sendQRImage(qrMessage);

        return 1;

    }

    @Override
    public Optional<List<String>> getQRsContentByOrganization(String organizationId) {
        return Optional.of(dynamicQRRepository.getByUniversityId(organizationId).stream()
                .map(DynamicQR::getContent)
                .collect(Collectors.toList()));
    }
}
