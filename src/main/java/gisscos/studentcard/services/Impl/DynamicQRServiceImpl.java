package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.User;
import gisscos.studentcard.entities.dto.DynamicQRDTO;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.repositories.DynamicQRRepository;
import gisscos.studentcard.repositories.UserRepository;
import gisscos.studentcard.services.DynamicQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DynamicQRServiceImpl implements DynamicQRService {

    @Autowired
    DynamicQRRepository dynamicQRRepository;

    @Autowired
    UserRepository userRepository;


    /**
     * Метод получения информации о динамическом QR-коде
     * @param userToken - токен пользователя
     * @return DynamicQR
     * todo: в качестве контента сейчас применяется строка "someContent". Нужно заменить на реальные данные (от ГИС СЦОС)
     * */
    @Override
    public Optional<DynamicQR> getInfo(String userToken) {
        User currentUser = userRepository.getByToken(userToken).get();
        Optional<DynamicQR> dynamicQR = dynamicQRRepository.getByUserId(currentUser.getId());
        if (dynamicQR.isPresent()){
            return dynamicQR;
        }
        return Optional.of(dynamicQRRepository.save(
                new DynamicQR(currentUser.getId(), currentUser.getUniversityId(),
                        QRStatus.NEW, "someContent")));
    }



    @Override
    public ResponseEntity<Resource> downloadQRAsFile(String userToken) {

        return null;
    }

    @Override
    public Optional<DynamicQR> editPermanentQR(DynamicQRDTO dynamicQRDTO) {
        return Optional.empty();
    }
}
