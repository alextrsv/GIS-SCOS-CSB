package ru.edu.online.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.enums.QRDataVerifyStatus;
import ru.edu.online.services.IPermanentQRService;
import ru.edu.online.services.IStudentService;
import ru.edu.online.services.IUserService;
import ru.edu.online.utils.HashingUtil;

import java.awt.image.BufferedImage;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiceImpl implements IPermanentQRService {

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final IUserService userServiceImpl;

    private final IStudentService studentServiceImpl;


    @Autowired
    public PermanentQRServiceImpl(VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, IUserService userServiceImpl, IStudentService IStudentService) {
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.userServiceImpl = userServiceImpl;
        this.studentServiceImpl = IStudentService;
    }

    @Override
    public Optional<Resource> downloadQRAsFile(String userId) {
        String content = null;

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

            String userEmail = scosUser.get().getEmail();
            Optional<StudentDTO> vamStudent;
            if (userEmail.equals("stud_bilet_01@dev.online.edu.ru"))
                vamStudent = vamRestClient.makeGetStudentByEmailRequestFor01(userEmail);
            else
                vamStudent = vamRestClient.makeGetStudentByEmailRequest(userEmail);
            if (vamStudent.isPresent()) {
                vamStudent.get().setScos_id(scosUser.get().getUser_id());
                content = studentServiceImpl.makeContent(vamStudent.get());
            }
            else{
                content = userServiceImpl.makeContent(scosUser.get());
            }
        }
//        5. Если запрос к ВАМу ничего не возвращает - это не студент, работаю с пользователем из СЦОСА
        else{
            content = userServiceImpl.makeContent(scosUser.get());
        }

        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return Converter.getResource(qrCodeImage);
    }

    @Override
    public Optional<QRDataVerifyStatus> verifyData(String userId, String dataHash) {
        QRDataVerifyStatus verifyStatus = null;

        Optional<UserDTO> scosUser = gisScosApiRestClient.makeGetUserRequest(userId);
        if (scosUser.isEmpty()) return Optional.empty();
        String userEmail = scosUser.get().getEmail();
//        3. Запрос к ВАМу на получение студента по почте (снилсу)
        Optional<StudentDTO> vamStudent;
        if(userEmail.equals("stud_bilet_01@dev.online.edu.ru"))
            vamStudent = vamRestClient.makeGetStudentByEmailRequestFor01(userEmail);
        else
            vamStudent = vamRestClient.makeGetStudentByEmailRequest(userEmail);
//        4. Если запрос к ВАМу вернул объект студента - работаю с этим объектом.
        if (vamStudent.isPresent()) {
            vamStudent.get().setScos_id(scosUser.get().getUser_id());
            verifyStatus = verifyStudentData(vamStudent.get(), dataHash);
        }
//        5. Если запрос к ВАМу ничего не возвращает - это не студент, работаю с пользователем из СЦОСА
        else{
            verifyStatus = verufyUserData(scosUser.get(), dataHash);
        }
        return Optional.ofNullable(verifyStatus);
    }

    private QRDataVerifyStatus verufyUserData(UserDTO userDTO, String hash) {
        try {
            String newHash = HashingUtil.getHash(userServiceImpl.makeUsefullContent(userDTO));
            if (newHash.equals(hash)) return QRDataVerifyStatus.OK;
            else return QRDataVerifyStatus.INVALID;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private QRDataVerifyStatus verifyStudentData(StudentDTO studentDTO, String hash) {
        try {
            String lastHash = HashingUtil.getHash(studentServiceImpl.makeUsefullContent(studentDTO));
            if (lastHash.equals(hash)) return QRDataVerifyStatus.OK;
            else return QRDataVerifyStatus.INVALID;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Optional<StudentDTO> getStudent(String userId){
        return vamRestClient.makeGetStudentRequest(userId);
    }


}
