package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.UserDTO;
import gisscos.studentcard.entities.enums.QRDataVerifyStatus;
import gisscos.studentcard.services.IPermanentQRService;
import gisscos.studentcard.services.IStudentService;
import gisscos.studentcard.services.IUserService;
import gisscos.studentcard.utils.HashingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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

    private final IUserService IUserService;

    private final IStudentService IStudentService;


    @Autowired
    public PermanentQRServiceImpl(VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, IUserService IUserService, IStudentService IStudentService) {
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.IUserService = IUserService;
        this.IStudentService = IStudentService;
    }

    @Override
    public Optional<Resource> downloadQRAsFile(String userId) {
        String content = null;
          /*нужно понять, пользователь с id - студент или обычный пользователь?
        1. делаем запрос на получение студента - если ответ не пустой, то это студент, работаем с ним
        2. иначе, если ответ пустой - это не студент. Делаем запрос к гис сцосу.
        2.1. Если ответ пустой, то это выдаем ошибку - пользователя нет
        2.2. Иначе, если ответ не пустой - работаем с пользователем.
        */
        Optional<StudentDTO> studentDTOWrapper = getStudent(userId);
        if (studentDTOWrapper.isPresent())
            content = IStudentService.makeContent(studentDTOWrapper.get());
        else{
            Optional<UserDTO> userWrapper = gisScosApiRestClient.makeGetUserRequest(userId);
            if (userWrapper.isPresent()){
                content = IUserService.makeContent(userWrapper.get());
            }else return Optional.empty();
        }

        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return Converter.getResource(qrCodeImage);
    }

    @Override
    public Optional<QRDataVerifyStatus> verifyData(String userId, String dataHash) {
        QRDataVerifyStatus verifyStatus = null;
        Optional<StudentDTO> studentDTOWrapper = getStudent(userId);
        if (studentDTOWrapper.isPresent())
            verifyStatus = verifyStudentData(studentDTOWrapper.get(), dataHash);
        else{
            Optional<UserDTO> userWrapper = gisScosApiRestClient.makeGetUserRequest(userId);
            if (userWrapper.isPresent()){
                verifyStatus = verufyUserData(userWrapper.get(), dataHash);
            }else return Optional.empty();
        }
        return Optional.ofNullable(verifyStatus);
    }

    private QRDataVerifyStatus verufyUserData(UserDTO userDTO, String hash) {
        try {
            String newHash = HashingUtil.getHash(IUserService.makeUsefullContent(userDTO));
            if (newHash.equals(hash)) return QRDataVerifyStatus.OK;
            else return QRDataVerifyStatus.INVALID;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private QRDataVerifyStatus verifyStudentData(StudentDTO studentDTO, String hash) {
        try {
            if (HashingUtil.getHash(IStudentService.makeUsefullContent(studentDTO)).equals(hash)) return QRDataVerifyStatus.OK;
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
