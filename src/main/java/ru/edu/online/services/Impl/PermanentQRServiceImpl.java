package ru.edu.online.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.QRUser;
import ru.edu.online.entities.dto.PermanentUserQRDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.enums.QRDataVerifyStatus;
import ru.edu.online.services.IPermanentQRService;
import ru.edu.online.services.IQRUserService;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.utils.HashingUtil;

import java.awt.image.BufferedImage;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiceImpl implements IPermanentQRService {

    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final StudentServiceImpl studentServiceImpl;

    private final UserServiceImpl userServiceImpl;

    private IQRUserService IQRUserServiceImps;



    @Autowired
    public PermanentQRServiceImpl(GisScosApiRestClient gisScosApiRestClient,
                                  StudentServiceImpl studentServiceImpl,
                                  UserServiceImpl userServiceImpl,
                                  IScosAPIService scosAPIService,
                                  VamRestClient vamRestClient) {

        this.gisScosApiRestClient = gisScosApiRestClient;
        this.studentServiceImpl = studentServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.scosAPIService = scosAPIService;
        this.vamRestClient = vamRestClient;
    }


    private Optional<QRUser> getDefinedRole(String userId) {
        /*
         * 1. Получаю ID из СЦОСа - userId
         * 2. Запрос к СЦОСу на полуение объекта пользователя
         * 3. Запрос к ВАМу на получение студента по почте (снилсу)
         * 4. Если запрос к ВАМу вернул объект студента - работаю с этим объектом.
         * 5. Если запрос к ВАМу ничего не возвращает - это не студент, работаю с пользователем из СЦОСА
         */
        //2.Запрос к СЦОСу на полуение объекта пользователя

        Optional<UserDTO> scosUser = gisScosApiRestClient.makeGetUserRequest(userId);
        if (scosUser.isEmpty()) return Optional.empty();

        if (scosUser.get().getEmail() != null) {
            scosUser.get().setPhoto_url(
                    Arrays.stream(
                                    scosAPIService.getUserByFIO(
                                            scosUser.get().getFirst_name(),
                                            scosUser.get().getLast_name()
                                    ).orElseThrow().getData()
                            )
                            .filter(user -> user.getUser_id().equals(scosUser.get().getUser_id()))
                            .findFirst()
                            .orElseThrow()
                            .getPhoto_url());
            String userEmail = scosUser.get().getEmail();
            Optional<StudentDTO> vamStudent;
            if (userEmail.equals("stud_bilet_01@dev.online.edu.ru"))
                vamStudent = vamRestClient.makeGetStudentByEmailRequestFor01(userEmail);
            else
                vamStudent = vamRestClient.makeGetStudentByEmailRequest(userEmail);
            if (vamStudent.isPresent()) {
                vamStudent.get().setPhoto_url(scosUser.get().getPhoto_url());
                vamStudent.get().setScos_id(scosUser.get().getUser_id());
                this.IQRUserServiceImps = studentServiceImpl;
                return Optional.of(vamStudent.get());
            }
            else{
                this.IQRUserServiceImps = userServiceImpl;
                return Optional.of(scosUser.get());
            }
        }
        else{
            this.IQRUserServiceImps = userServiceImpl;
            return Optional.of(scosUser.get());
        }
    }


    @Override
    public Optional<Resource> downloadQRAsFile(String userId) {
        Optional<QRUser> qrUser =  getDefinedRole(userId);
//        String content = qrUserServiceImps.getContentWithHash(qrUser.get());
        String content = String.format("{\"id\":\"%s\", \"hash\":\"%s\"}", userId, IQRUserServiceImps.getHash(qrUser.orElseThrow()));
        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return Converter.getResource(qrCodeImage);
    }

    @Override
    public Optional<QRDataVerifyStatus> verifyData(String userId, String dataHash) {
        Optional<QRUser> qrUser =  getDefinedRole(userId);
        try {
            String newHash = HashingUtil.getHash(IQRUserServiceImps.getFullStaticQRPayload(qrUser.orElseThrow()).toString());
            if (newHash.equals(dataHash)) return Optional.of(QRDataVerifyStatus.OK);
            else return Optional.of(QRDataVerifyStatus.INVALID);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<PermanentUserQRDTO> getFullUserInfo(String userId) {
        Optional<QRUser> qrUser = getDefinedRole(userId);
        return Optional.ofNullable(IQRUserServiceImps.getFullStaticQRPayload(qrUser.orElseThrow()));
    }

    @Override
    public Optional<PermanentUserQRDTO> getAbbreviatedStaticQRPayload(String userId) {
        Optional<QRUser> qrUser = getDefinedRole(userId);
        return Optional.ofNullable(IQRUserServiceImps.getAbbreviatedStaticQRPayload(qrUser.orElseThrow()));
    }
}
